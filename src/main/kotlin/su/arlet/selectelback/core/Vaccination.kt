package su.arlet.selectelback.core


import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "vaccinations")
data class Vaccination(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "petId")
    var pet: Pet,

    var vaccinationDate: LocalDate,
    var name: String,
    var description: String?
)