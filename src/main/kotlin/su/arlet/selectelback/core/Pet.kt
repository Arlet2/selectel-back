package su.arlet.selectelback.core

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate

@Entity
@Table(name = "pets")
data class Pet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(cascade = [CascadeType.REMOVE])
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "ownerId")
    var owner: User,

    @ManyToOne
    @JoinColumn(name = "petTypeId")
    var petType: PetType,

    @ManyToOne
    @JoinColumn(name = "bloodTypeId")
    var bloodType: BloodType,

    var name: String,
    var description: String?,
    var birthday: LocalDate?,
    var weight: Double?
)