package at.darioseidl.aoprofiling

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "aoprofiling")
class ProfilingProperties {

    enum class Mode {
        PLAIN, TREE, VERBOSE
    }

    /** Set to true to enable method tracing. */
    var enabled: Boolean = true

    /** The display mode. TREE adds line drawings to the call tree, VERBOSE prints to multiple lines. */
    var mode: Mode = Mode.TREE

    /** Strings will be truncated to at most this number of characters, unless mode is VERBOSE. */
    var truncate: Int = 100

    /** A comma-separated list of target.method names to ignore. */
    var ignore: String = ""

    /**
     * The order of the ProfilingAspect, should be lower than the transaction advisor order.
     */
    var profilingAspectOrder = -1

    /**
     * The order of the ProfilingSummaryAspect, should be lower than the profilingAspectOrder.
     */
    var profilingSummaryAspectOrder = -2
}
