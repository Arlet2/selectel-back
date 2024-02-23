package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import su.arlet.selectelback.core.Location
import su.arlet.selectelback.core.PetType

interface PetTypeRepo : JpaRepository<PetType, Long> {
    @Query("SELECT DISTINCT p.type FROM PetType p")
    fun findTypes(): List<String>
    fun findByType(type: String): List<PetType>?
}