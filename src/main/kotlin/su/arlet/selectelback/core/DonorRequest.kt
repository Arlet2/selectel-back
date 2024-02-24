package su.arlet.selectelback.core

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "donor_requests")
class DonorRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @ManyToOne
    var user: User,
    var description: String,
    var vetAddress: String,
    @ManyToOne
    @JoinColumn(name = "pet_type_id")
    var petType: PetType,
    @ManyToOne
    @JoinColumn(name = "blood_type_id")
    var bloodType: BloodType,
    var bloodAmountMl: Double,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var availableUntil: LocalDate?,
)