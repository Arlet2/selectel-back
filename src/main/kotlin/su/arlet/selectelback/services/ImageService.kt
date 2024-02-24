package su.arlet.selectelback.services

import at.favre.lib.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Component

@Component
class ImageService {
    val HASH_COST = 10


    fun hashFilename(fileName: String): String {
        return BCrypt.withDefaults().hashToString(HASH_COST, fileName.toCharArray())
    }
}