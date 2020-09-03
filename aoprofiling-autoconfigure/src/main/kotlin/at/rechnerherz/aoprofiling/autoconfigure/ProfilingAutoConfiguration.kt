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
public class ProfilingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "at.rechnerherz.aoprofiling", name = ["enabled"], havingValue = "true")
    public fun profilingAspect(
        properties: ProfilingProperties,
    ): ProfilingAspect =
        ProfilingAspect(properties)

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "at.rechnerherz.aoprofiling", name = ["enabled"], havingValue = "true")
    public fun profilingSummaryAspect(
        profilingAspect: ProfilingAspect,
        properties: ProfilingProperties,
    ): ProfilingSummaryAspect =
        ProfilingSummaryAspect(properties, profilingAspect)

}
