package su.arlet.selectelback.controllers.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import su.arlet.selectelback.services.AuthService

@Component
class AuthFilter @Autowired constructor(private val authService: AuthService) :
    OncePerRequestFilter() {

    val ignoredURLs = arrayOf("\${api_path}/auth/login", "\${api_path}/auth/register")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.method == "OPTIONS") {
            filterChain.doFilter(request, response)
            return
        }

        authService.verifyToken(request)

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI in ignoredURLs
    }
}