package at.rechnerherz.aoprofiling

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "at.rechnerherz.aoprofiling")
public class ProfilingProperties {

    public enum class Mode {
        PLAIN, TREE, VERBOSE
    }

    /** Whether to enable aspect oriented profiling. */
    public var enabled: Boolean = false

    /** Display mode: TREE adds line drawings to the call tree, VERBOSE prints to multiple lines, PLAIN does neither. */
    public var mode: Mode = Mode.TREE

    /** Strings will be truncated to at most this number of characters, unless mode is VERBOSE. */
    public var truncate: Int = 100

    /** Comma-separated list of target.method names to ignore. */
    public var ignore: String = ""

    /**
     * Order of the ProfilingAspect. Should be lower than the transaction advisor order.
     */
    public var profilingAspectOrder: Int = -1

    /**
     * Order of the ProfilingSummaryAspect. Should be lower than the profilingAspectOrder.
     */
    public var profilingSummaryAspectOrder: Int = -2
}
