package at.rechnerherz.aoprofiling

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import java.util.*

/**
 * The [ProfilingAspect] traces the execution of public methods
 *
 * - within `org.springframework.stereotype.Service` classes
 *
 * - within `org.springframework.stereotype.Controller` classes
 *
 * - within `org.springframework.web.bind.annotation.RestController` classes
 *
 * - within `org.springframework.data.rest.webmvc.BasePathAwareController` classes
 *
 *     (this also includes `org.springframework.data.rest.webmvc.RepositoryRestController` classes)
 *
 * - within `org.springframework.data.repository.Repository` and sub-classes
 *
 * - within `org.springframework.data.rest.core.annotation.RepositoryEventHandler` classes
 *
 * When using Spring proxy-based AOP, it only works for non-final public methods of non-final public Spring beans,
 * when they are not self invocated.
 *
 * To avoid any overhead in production, only enable it in development.
 *
 * To make sure that the ProfilingAspect is executed before (around) transactions, set the
 * transaction advisor order to a lower priority (higher number) than the [ProfilingProperties.profilingAspectOrder],
 * e.g. with `@EnableTransactionManagement(order = 0)`.
 *
 * [Advising transactional operations](https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#transaction-declarative-applying-more-than-just-tx-advice)
 */
@Aspect
@Component
class ProfilingAspect(
    private val properties: ProfilingProperties
) : Ordered {

    private val log = LoggerFactory.getLogger(ProfilingAspect::class.java)

    private val threadLocalStack: ThreadLocal<Stack<ProfilingInfo>> =
        ThreadLocal.withInitial { Stack<ProfilingInfo>() }

    private val threadLocalInfos: ThreadLocal<LinkedMultiValueMap<String, ProfilingInfo>> =
        ThreadLocal.withInitial { LinkedMultiValueMap<String, ProfilingInfo>() }

    private val verbose: Boolean
        get() = properties.mode == ProfilingProperties.Mode.VERBOSE

    private val tree: Boolean
        get() = properties.mode == ProfilingProperties.Mode.TREE

    override fun getOrder(): Int =
        properties.profilingAspectOrder

    /*------------------------------------*\
     * Pointcuts
    \*------------------------------------*/

    @Pointcut("execution(public * *(..))")
    fun publicMethod() {}

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    fun withinService() {}

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    fun withinController() {}

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    fun withinRestController() {}

    @Pointcut(
        "within(@org.springframework.data.rest.webmvc.BasePathAwareController *)" +
                " && !within(org.springframework.data.rest.webmvc.*)"
    )
    fun withinBasePathAwareController() {}

    @Pointcut(
        "within(@org.springframework.data.rest.webmvc.RepositoryRestController *)" +
                " && !within(org.springframework.data.rest.webmvc.*)"
    )
    fun withinRepositoryRestController() {}

    @Pointcut("within(org.springframework.data.repository.Repository+)")
    fun withinRepository() {}

    @Pointcut("within(@org.springframework.data.rest.core.annotation.RepositoryEventHandler *)")
    fun withinRepositoryEventHandler() {}

    @Pointcut(
        "within(at.rechnerherz.aoprofiling.NoProfiling *)" +
                " || @annotation(at.rechnerherz.aoprofiling.NoProfiling)"
    )
    fun noProfiling() {}

    @Pointcut(
        "execution(public String toString())" +
                " || execution(public int hashCode())" +
                " || execution(public boolean equals(Object))"
    )
    fun boilerplate() {}

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
     * Summary Methods
    \*------------------------------------*/

    fun clear() {
        threadLocalInfos.get().clear()
        threadLocalStack.get().clear()
    }

    fun summary(): String {
        val infoMap: LinkedMultiValueMap<String, ProfilingInfo> = threadLocalInfos.get()
        val totalMillis: Long = infoMap.values.sumOf { infos -> infos.sumOf { it.millis } }
        val sb = StringBuilder()
        sb.append("Total time measured on thread '")
        sb.append(Thread.currentThread().name)
        sb.append("': ")
        sb.append(totalMillis)
        sb.append(" ms\n")

        sb.append(HR)
        sb.append("    #     ms    avg     %    method\n")
        sb.append(HR)
        for ((_: String, infos: List<ProfilingInfo>) in infoMap) {
            val count: Int = infos.size
            val ms: Long = infos.sumOf { it.millis }
            val avg: Int = (ms.toDouble() / count).toInt()
            val percent: Int = (ms.toDouble() / totalMillis * 100.0).toInt()
            sb.append(
                String.format(
                    "%5d %6d %6d %5d    %s%s%n",
                    count, ms, avg, percent,
                    summaryTreeDrawing(infos.first().callStack.size),
                    infos.first().signature
                )
            )
        }
        sb.append(HR)
        return sb.toString()
    }

    /*------------------------------------*\
     * Helper Methods
    \*------------------------------------*/

    private fun profileMethod(joinPoint: ProceedingJoinPoint): Any? {
        val targetAndMethodName = joinPoint.targetAndMethodName()

        if (!properties.enabled || properties.ignore.split(",").contains(targetAndMethodName))
            return joinPoint.proceed()

        val stack: Stack<ProfilingInfo> = threadLocalStack.get()
        val signature = joinPoint.signature(verbose, properties.truncate)
        var returnValue: Any? = "[no return value obtained]"

        try {
            stack.start(targetAndMethodName)

            if (log.isTraceEnabled) {
                val tree = treeDrawing(stack.size)
                log.trace("$tree$signature")
            }

            returnValue = joinPoint.proceed()
            return returnValue
        } finally {
            val executionMillis: Long = stack.stop()

            if (log.isTraceEnabled) {
                val tree = treeDrawing(stack.size, true)
                val returnValueString = joinPoint.returnValueToString(returnValue, verbose, properties.truncate)
                val separator = if (verbose) "\n" else ""
                log.trace("$tree$signature returned $returnValueString$separator — execution time: $executionMillis ms")
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

    private fun summaryTreeDrawing(size: Int): String =
        if (tree) ((if (size > 0) "├" + "─".repeat(size) else "└") + " ") else ""

    companion object {
        const val HR = "────────────────────────────────────────────────────────────────────────────────\n"
    }

}
