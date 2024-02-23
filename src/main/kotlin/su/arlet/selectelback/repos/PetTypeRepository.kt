package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import su.arlet.selectelback.core.PetType

interface PetTypeRepository : JpaRepository<PetType, Long> {}