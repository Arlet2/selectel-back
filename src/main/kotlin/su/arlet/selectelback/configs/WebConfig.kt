package su.arlet.selectelback.configs


import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@SecurityScheme(
    name = "Authorization",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000", "https://petdonor.ru", "http://127.0.0.1:3000")
            .allowedMethods("POST", "GET", "PUT", "DELETE", "PATCH")
            .allowCredentials(true).maxAge(3600)
    }

    @Bean
    fun customOpenAPI(): OpenAPI {
        val urls = arrayOf("https://api.petdonor.ru", "http://localhost:8080")
        val servers = mutableListOf<Server>()

        var server: Server
        for (url in urls) {
            server = Server()
            server.url = url
            servers.add(server)
        }


        return OpenAPI().servers(servers)
    }
}