package su.arlet.selectelback.controllers.filters

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import su.arlet.selectelback.services.AuthService

@Configuration
class FiltersConfiguration {

    @Autowired
    @Bean
    fun createAuthFilter(
        authService: AuthService
    ): FilterRegistrationBean<AuthFilter> {
        val filter: FilterRegistrationBean<AuthFilter> = FilterRegistrationBean<AuthFilter>()

        filter.filter = AuthFilter(authService)
        filter.addUrlPatterns("/*")

        return filter
    }
}