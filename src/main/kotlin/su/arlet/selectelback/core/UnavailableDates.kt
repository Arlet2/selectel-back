package su.arlet.selectelback.core

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name="unavailable_dates")
class UnavailableDates(
    @Id
    val userID: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
)