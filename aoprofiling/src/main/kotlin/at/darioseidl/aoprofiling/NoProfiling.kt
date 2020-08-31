package at.rechnerherz.aoprofiling

/**
 * If a method is annotated with [NoProfiling] it will not be advised by the [ProfilingAspect].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class NoProfiling
