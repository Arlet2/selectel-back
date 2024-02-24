package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import su.arlet.selectelback.core.PetType

interface PetTypeRepo : JpaRepository<PetType, Long> {
    fun findByBreedIsNull(): List<PetType>
    fun findByType(type: String): List<PetType>?
}