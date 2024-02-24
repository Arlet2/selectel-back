package su.arlet.selectelback.core

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name="donor_requests")
class DonorRequest (
    @Id
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
    var bloodAmountMl : Double,
    var availableUntil: LocalDate?,
    )