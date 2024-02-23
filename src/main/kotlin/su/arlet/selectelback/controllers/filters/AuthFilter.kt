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

//        val jwt: Optional<String> = headersService.getJWTFromHeader(request)
//
//        if (jwt.isEmpty()) {
//            println("No jwt request")
//
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Ошибка авторизации на сервере")
//            return
//        }
//
//        if (!authService.isJWTValid(jwt.get())) {
//            println("Request with invalid jwt")
//
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Ошибка авторизации на сервере")
//            return
//        }

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI in ignoredURLs
    }
}