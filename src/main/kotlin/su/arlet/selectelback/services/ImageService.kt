package su.arlet.selectelback.services

import at.favre.lib.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class ImageService {
    val HASH_COST = 10


    fun hashFilename(fileName: String): String {
        return Base64.getEncoder().encodeToString(BCrypt.withDefaults().hashToString(HASH_COST, fileName.toCharArray()).toByteArray())
    }
}