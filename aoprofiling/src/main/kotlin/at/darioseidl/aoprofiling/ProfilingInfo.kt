package at.rechnerherz.aoprofiling

data class ProfilingInfo(
    val callStack: List<String>,
    val signature: String,
    val millis: Long
) {
    fun callStackSignature(): String =
        if (callStack.isEmpty()) signature else callStack.joinToString(separator = "---") + "---" + signature
}
