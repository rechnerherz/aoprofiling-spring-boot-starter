package at.darioseidl.aoprofiling

import mu.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import java.util.*

/**
 * The [ProfilingAspect] traces the execution of public methods
 *
 * - within [org.springframework.stereotype.Service] classes
 *
 * - within [org.springframework.stereotype.Controller] classes
 *
 * - within [org.springframework.web.bind.annotation.RestController] classes
 *
 * - within [org.springframework.data.rest.webmvc.BasePathAwareController] classes
 *
 *     (this also includes [org.springframework.data.rest.webmvc.RepositoryRestController] classes)
 *
 * - within [org.springframework.data.repository.Repository] and sub-classes
 *
 * - within [org.springframework.data.rest.core.annotation.RepositoryEventHandler] classes
 *
 * When using Spring proxy-based AOP, it only works for non-final public methods of non-final public Spring beans,
 * when they are not self invocated.
 *
 * To avoid any overhead in production, it is only registered in development.
 *
 * The order is set to run before the transaction advice.
 *
 * [Advising transactional operations](https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#transaction-declarative-applying-more-than-just-tx-advice)
 */
@Aspect
@Component
@Order(-1)
class ProfilingAspect(
    internal val properties: ProfilingProperties
) {

    internal val log = KotlinLogging.logger {}

    internal val threadLocalStack: ThreadLocal<Stack<ProfilingInfo>> =
        ThreadLocal.withInitial { Stack<ProfilingInfo>() }

    internal val threadLocalInfos: ThreadLocal<LinkedMultiValueMap<String, ProfilingInfo>> =
        ThreadLocal.withInitial { LinkedMultiValueMap<String, ProfilingInfo>() }

    internal fun clear() {
        threadLocalInfos.get().clear()
        threadLocalStack.get().clear()
    }

    private val enabled: Boolean
        get() = properties.enabled

    private val verbose: Boolean
        get() = properties.mode == ProfilingProperties.Mode.VERBOSE

    private val tree: Boolean
        get() = properties.mode == ProfilingProperties.Mode.TREE

    private val truncate: Int
        get() = properties.truncate

    /*------------------------------------*\
     * Pointcuts
    \*------------------------------------*/

    @Pointcut("execution(public * *(..))")
    fun publicMethod() {
    }

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    fun withinService() {
    }

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    fun withinController() {
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    fun withinRestController() {
    }

    @Pointcut(
        "within(@org.springframework.data.rest.webmvc.BasePathAwareController *)" +
                " && !within(org.springframework.data.rest.webmvc.*)"
    )
    fun withinBasePathAwareController() {
    }

    @Pointcut(
        "within(@org.springframework.data.rest.webmvc.RepositoryRestController *)" +
                " && !within(org.springframework.data.rest.webmvc.*)"
    )
    fun withinRepositoryRestController() {
    }

    @Pointcut("within(org.springframework.data.repository.Repository+)")
    fun withinRepository() {
    }

    @Pointcut("within(@org.springframework.data.rest.core.annotation.RepositoryEventHandler *)")
    fun withinRepositoryEventHandler() {
    }

    @Pointcut(
        "within(@at.darioseidl.aop.NoProfiling *)" +
                " || @annotation(at.darioseidl.aop.NoProfiling)"
    )
    fun noProfiling() {
    }

    @Pointcut(
        "execution(public String toString())" +
                " || execution(public int hashCode())" +
                " || execution(public boolean equals(Object))"
    )
    fun boilerplate() {
    }

    /*------------------------------------*\
     * Advices
    \*------------------------------------*/

    @Around("publicMethod() && !noProfiling() && !boilerplate() && withinService()")
    fun profileServiceMethods(joinPoint: ProceedingJoinPoint): Any? =
        profileMethod(joinPoint)

    @Around("publicMethod() && !noProfiling() && !boilerplate() && withinController()")
    fun profileControllerMethods(joinPoint: ProceedingJoinPoint): Any? =
        profileMethod(joinPoint)

    @Around("publicMethod() && !noProfiling() && !boilerplate() && withinRestController()")
    fun profileRestControllerMethods(joinPoint: ProceedingJoinPoint): Any? =
        profileMethod(joinPoint)

    @Around("publicMethod() && !noProfiling() && !boilerplate() && withinBasePathAwareController()")
    fun profileBasePathAwareControllerMethods(joinPoint: ProceedingJoinPoint): Any? =
        profileMethod(joinPoint)

    @Around("publicMethod() && !noProfiling() && !boilerplate() && withinRepositoryRestController()")
    fun profileRepositoryRestControllerMethods(joinPoint: ProceedingJoinPoint): Any? =
        profileMethod(joinPoint)

    @Around("publicMethod() && !noProfiling() && !boilerplate() && withinRepository()")
    fun profileRepositoryMethods(joinPoint: ProceedingJoinPoint): Any? =
        profileMethod(joinPoint)

    @Around("publicMethod() && !noProfiling() && !boilerplate() && withinRepositoryEventHandler()")
    fun profileRepositoryEventHandlerMethods(joinPoint: ProceedingJoinPoint): Any? =
        profileMethod(joinPoint)

    /*------------------------------------*\
     * Helper Methods
    \*------------------------------------*/

    private fun profileMethod(joinPoint: ProceedingJoinPoint): Any? {
        val targetAndMethodName = joinPoint.targetAndMethodName()

        if (!enabled || properties.ignore.split(",").contains(targetAndMethodName))
            return joinPoint.proceed()

        val stack: Stack<ProfilingInfo> = threadLocalStack.get()
        val signature = joinPoint.signature(verbose, truncate)
        var returnValue: Any? = "[no return value obtained]"

        try {
            stack.start(targetAndMethodName)

            log.trace {
                val tree = treeDrawing(stack.size)
                "$tree$signature"
            }

            returnValue = joinPoint.proceed()
            return returnValue
        } finally {
            val executionMillis: Long = stack.stop()

            log.trace {
                val tree = treeDrawing(stack.size, true)
                val returnValueString = joinPoint.returnValueToString(returnValue, verbose, truncate)
                val separator = if (verbose) "\n" else ""
                "$tree$signature returned $returnValueString$separator — execution time: $executionMillis ms"
            }

            stack.done()
        }
    }

    private fun Stack<ProfilingInfo>.start(signature: String) {
        val stackSignatures: List<String> = map { it.signature }
        push(ProfilingInfo(stackSignatures, signature, System.currentTimeMillis()))
    }

    private fun Stack<ProfilingInfo>.stop(): Long {
        val (callStack: List<String>, signature: String, startTimeMillis: Long) = peek()
        val executionMillis: Long = System.currentTimeMillis() - startTimeMillis
        val infos: LinkedMultiValueMap<String, ProfilingInfo> = threadLocalInfos.get()
        val info = ProfilingInfo(callStack, signature, executionMillis)
        infos.add(info.callStackSignature(), info)
        return executionMillis
    }

    private fun Stack<ProfilingInfo>.done() {
        pop()
    }

    private fun treeDrawing(size: Int, after: Boolean = false): String =
        if (tree) ((if (size > 1) "├" + "─".repeat(size - 1) else if (after) "└" else "┌") + " ") else ""

}
