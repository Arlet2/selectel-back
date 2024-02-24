package su.arlet.selectelback.core


import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate

@Entity
@Table(name = "vaccinations")
data class Vaccination(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "petId")
    var pet: Pet,

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var vaccinationDate: LocalDate,
    var name: String,
    var description: String?
)