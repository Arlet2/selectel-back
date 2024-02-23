package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import su.arlet.selectelback.core.BloodType

interface BloodTypeRepository : JpaRepository<BloodType, Long> {}