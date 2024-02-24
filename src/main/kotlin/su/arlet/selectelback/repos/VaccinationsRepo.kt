package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import su.arlet.selectelback.core.Vaccination

interface VaccinationsRepo : JpaRepository<Vaccination, Long> {
    fun findByPetId(petId: Long): List<Vaccination>
}
