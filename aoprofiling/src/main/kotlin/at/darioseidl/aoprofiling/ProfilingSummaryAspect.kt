package at.darioseidl.aoprofiling

import mu.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * The [ProfilingSummaryAspect] prints a profiling summary after execution of a method annotated with [ProfilingSummary].
 *
 * This aspect is separated from the [ProfilingAspect] and has a lower order,
 * to make sure that the summary is printed after profiling the advised method.
 */
@Aspect
@Component
@Order(-2)
class ProfilingSummaryAspect(
    private val properties: ProfilingProperties,
    private val profilingAspect: ProfilingAspect
) {
    private val log = KotlinLogging.logger {}

    @Around("@annotation(at.darioseidl.aop.ProfilingSummary) && @annotation(profilingSummary)")
    fun printProfileSummary(joinPoint: ProceedingJoinPoint, profilingSummary: ProfilingSummary): Any? {
        if (profilingSummary.clearBefore)
            profilingAspect.clear()

        try {
            return joinPoint.proceed()
        } finally {
            if (properties.enabled && log.isDebugEnabled)
                log.debug(profilingAspect.summary())
        }
    }


}
