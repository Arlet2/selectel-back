package su.arlet.selectelback.core

import com.fasterxml.jackson.annotation.JsonFormat
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

    @ManyToOne
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var birthday: LocalDate?,
    var weight: Double?,
    var avatar: String = "https://petdonor.ru/avatar/default-avatar-pet.jpg",
)