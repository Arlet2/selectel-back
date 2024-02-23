package su.arlet.selectelback.controllers.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import su.arlet.selectelback.exceptions.UnauthorizedError
import su.arlet.selectelback.services.AuthService

class AuthFilter constructor(private val authService: AuthService) :
    GenericFilterBean() {

    override fun doFilter(
        servletRequest: ServletRequest,
        servletResponse: ServletResponse,
        filterChain: FilterChain
    ) {
        val request = servletRequest as HttpServletRequest
        val response = servletResponse as HttpServletResponse

        if (request.method == "OPTIONS") {
            filterChain.doFilter(request, response)
            return
        }

        try {
            authService.verifyToken(request)
        } catch (e : Exception) {
            println(e)
            throw UnauthorizedError()
        }

        filterChain.doFilter(request, response)
    }
}