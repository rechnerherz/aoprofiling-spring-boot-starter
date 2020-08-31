package at.darioseidl.aoprofiling

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Pattern
import javax.validation.constraints.Positive

@Component
@Validated
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
    @Positive
    var truncate: Int = 100

    /** A comma-separated list of target.method names to ignore. */
    @Pattern(regexp = """|(\S+(,\S+)*)""")
    var ignore: String = ""
}
