package at.darioseidl.aoprofiling

/**
 * If a method is annotated with [ProfilingSummary] a profiling summary will be printed after its execution.
 *
 * Only works on public methods of Spring beans.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class ProfilingSummary(

    /**
     * When [clearBefore] is true, the statistics will be cleared before the method is profiled,
     * i.e. only the method itself and nested method calls will be printed in the summary.
     *
     * Otherwise, the summary may contain earlier executions of (unrelated) method calls.
     */
    val clearBefore: Boolean = true

)
