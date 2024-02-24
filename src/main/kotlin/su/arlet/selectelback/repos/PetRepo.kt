package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import su.arlet.selectelback.core.Pet

interface PetRepo : JpaRepository<Pet, Long> {
    fun findByOwnerId(ownerId: Long) : List<Pet>
}
