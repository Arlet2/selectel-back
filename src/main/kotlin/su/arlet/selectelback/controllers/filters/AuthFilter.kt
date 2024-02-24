package su.arlet.selectelback.controllers.filters

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import su.arlet.selectelback.services.AuthService

class AuthFilter(private val authService: AuthService) : Filter {

    override fun init(filterConfig: FilterConfig?) {}

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
        } else if (excludeUrl(request.requestURI)) {
            filterChain.doFilter(servletRequest, servletResponse)
            return
        }

        try {
            //authService.verifyToken(request)
        } catch (e: Exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        }

        filterChain.doFilter(request, response)
    }

    override fun destroy() {}

    private fun excludeUrl(requestUri: String): Boolean {
        // Define the URLs that should be excluded from filtering
        // Return true if the URL should be excluded, false otherwise
        return requestUri.startsWith("/back/api/v1/auth/")
                || requestUri.startsWith("/back/api/v1/location")
                || requestUri.startsWith("/back/api/v1/pets/types")
                || requestUri.startsWith("/back/api/v1/pets/breeds")
                || requestUri.startsWith("/back/api/v1/pets/blood_types")
                || requestUri.startsWith("/swagger")
                || requestUri.startsWith("/docs")
    }
}