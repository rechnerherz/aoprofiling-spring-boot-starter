package at.rechnerherz.aoprofiling.autoconfigure

import at.rechnerherz.aoprofiling.ProfilingAspect
import at.rechnerherz.aoprofiling.ProfilingProperties
import at.rechnerherz.aoprofiling.ProfilingSummaryAspect
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ProfilingProperties::class)
open class ProfilingAspectAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ["aoprofiling.enabled"], havingValue = "true")
    fun profilingAspect(
        properties: ProfilingProperties,
    ): ProfilingAspect =
            ProfilingAspect(properties)

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ["aoprofiling.enabled"], havingValue = "true")
    fun profilingSummaryAspect(
        profilingAspect: ProfilingAspect,
        properties: ProfilingProperties,
    ): ProfilingSummaryAspect =
            ProfilingSummaryAspect(properties, profilingAspect)

}
