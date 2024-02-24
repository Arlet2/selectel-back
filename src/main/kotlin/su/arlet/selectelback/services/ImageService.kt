package su.arlet.selectelback.services

import at.favre.lib.crypto.bcrypt.BCrypt

class ImageService {
    val HASH_COST = 10


    fun hashFilename(fileName: String): String {
        return BCrypt.withDefaults().hashToString(HASH_COST, fileName.toCharArray())
    }
}