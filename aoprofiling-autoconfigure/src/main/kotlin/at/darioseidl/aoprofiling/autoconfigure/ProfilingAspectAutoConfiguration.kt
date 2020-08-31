package at.darioseidl.aoprofiling.autoconfigure

import at.darioseidl.aoprofiling.ProfilingAspect
import at.darioseidl.aoprofiling.ProfilingProperties
import at.darioseidl.aoprofiling.ProfilingSummaryAspect
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ProfilingProperties::class)
class ProfilingAspectAutoConfiguration {

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
