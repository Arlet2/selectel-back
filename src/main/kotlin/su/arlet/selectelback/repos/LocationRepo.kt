package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import su.arlet.selectelback.core.Location

interface LocationRepo : JpaRepository<Location, Long> {
    fun findByDistrictIsNull(): List<Location>
    fun findByCity(city: String): List<Location>?
}
