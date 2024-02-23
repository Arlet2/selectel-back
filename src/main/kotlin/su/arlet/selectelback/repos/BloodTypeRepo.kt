package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import su.arlet.selectelback.core.BloodType

interface BloodTypeRepo : JpaRepository<BloodType, Long> {
    fun findByTypeName(typeName: String): List<BloodType>?
}