package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import su.arlet.selectelback.core.Location

interface LocationRepo : JpaRepository<Location, Long> {
    @Query("SELECT DISTINCT l.city FROM Location l")
    fun findCities(): List<String>?
    fun findByCity(city: String): List<Location>?
}
