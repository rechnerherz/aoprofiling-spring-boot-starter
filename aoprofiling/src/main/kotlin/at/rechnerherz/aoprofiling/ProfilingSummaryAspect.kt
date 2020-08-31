package at.rechnerherz.aoprofiling

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

/**
 * The [ProfilingSummaryAspect] prints a profiling summary after execution of a method annotated with [ProfilingSummary].
 *
 * This aspect is separated from the [ProfilingAspect] and depends on it,
 * to make sure that the summary is printed after profiling the advised method.
 */
@Aspect
@Component
class ProfilingSummaryAspect(
    private val properties: ProfilingProperties,
    private val profilingAspect: ProfilingAspect
): Ordered {

    private val log = LoggerFactory.getLogger(ProfilingSummaryAspect::class.java)

    override fun getOrder(): Int =
        properties.profilingSummaryAspectOrder

    @Around("@annotation(at.rechnerherz.aoprofiling.ProfilingSummary) && @annotation(profilingSummary)")
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
