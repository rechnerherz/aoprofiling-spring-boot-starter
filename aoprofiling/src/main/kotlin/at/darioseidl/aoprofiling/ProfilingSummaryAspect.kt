package at.darioseidl.aoprofiling

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap

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
    private val profilingAspect: ProfilingAspect
) {

    private val log = profilingAspect.log

    private val enabled: Boolean
        get() = profilingAspect.properties.enabled

    private val tree: Boolean
        get() = profilingAspect.properties.mode == ProfilingProperties.Mode.TREE

    @Around("@annotation(at.darioseidl.aop.ProfilingSummary) && @annotation(profilingSummary)")
    fun printProfileSummary(joinPoint: ProceedingJoinPoint, profilingSummary: ProfilingSummary): Any? {
        if (profilingSummary.clearBefore)
            profilingAspect.clear()

        try {
            return joinPoint.proceed()
        } finally {
            if (enabled && log.isDebugEnabled)
                profilingAspect.log.debug(summary())
        }
    }

    fun summary(): String {
        val infoMap: LinkedMultiValueMap<String, ProfilingInfo> = profilingAspect.threadLocalInfos.get()
        val totalMillis: Long =
            profilingAspect.threadLocalInfos.get().values.sumByLong { it.sumByLong(ProfilingInfo::millis) }
        val sb = StringBuilder()
        sb.append("Total time measured on thread '")
        sb.append(Thread.currentThread().name)
        sb.append("': ")
        sb.append(totalMillis)
        sb.append(" ms\n")
        sb.append("─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────\n")
        sb.append("    #     ms    avg     %    method\n")
        sb.append("─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────\n")
        for ((_: String, infos: List<ProfilingInfo>) in infoMap) {
            val count: Int = infos.size
            val ms: Long = infos.sumByLong(ProfilingInfo::millis)
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
        sb.append("─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────\n")
        return sb.toString()
    }

    private fun summaryTreeDrawing(size: Int): String =
        if (tree) ((if (size > 0) "├" + "─".repeat(size) else "└") + " ") else ""

}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this)
        sum += selector(element)
    return sum
}
