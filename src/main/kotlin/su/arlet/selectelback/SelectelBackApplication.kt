package su.arlet.selectelback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import su.arlet.selectelback.services.AuthService
import su.arlet.selectelback.services.staticFilesPath
import java.nio.file.Files
import kotlin.io.path.Path

@SpringBootApplication
class SelectelBackApplication {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

}

fun main(args: Array<String>) {
    if (!Files.exists(staticFilesPath))
        Files.createDirectory(staticFilesPath)

    runApplication<SelectelBackApplication>(*args)
}
