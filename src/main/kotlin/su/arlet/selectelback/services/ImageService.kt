package su.arlet.selectelback.services

import at.favre.lib.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Component
import java.util.*

val defaultUserAvatarURL = "https://petdonor.ru/avatar/default-avatar-user.jpg"
val defaultPetAvatarURL = "https://petdonor.ru/avatar/default-avatar-user.jpg"

@Component
class ImageService {
    val HASH_COST = 10


    fun hashFilename(fileName: String): String {
        return Base64.getEncoder()
            .encodeToString(BCrypt.withDefaults().hashToString(HASH_COST, fileName.toCharArray()).toByteArray())
    }
}