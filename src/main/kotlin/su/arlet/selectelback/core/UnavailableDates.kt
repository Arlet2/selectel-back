package su.arlet.selectelback.core

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "unavailable_dates")
class UnavailableDates(
    @Id
    val userID: Long,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var startDate: LocalDate?,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var endDate: LocalDate?,
)