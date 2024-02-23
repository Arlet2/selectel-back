package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import su.arlet.selectelback.core.Location

interface LocationRepo : JpaRepository<Location, Long> {
    @Query("SELECT DISTINCT l.city FROM locations l")
    fun findDistinctCity(): List<String>
    fun findByCity(city: String): List<Location>?
}
