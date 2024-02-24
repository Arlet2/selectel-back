package su.arlet.selectelback.controllers.responses

import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import su.arlet.selectelback.core.BloodType
import su.arlet.selectelback.core.Pet
import su.arlet.selectelback.core.PetType
import su.arlet.selectelback.core.User
import java.time.LocalDate

class PetResponse(
    val id: Long,
    var owner: UserResponse,
    var petType: PetType,
    var bloodType: BloodType,
    var name: String,
    var description: String?,
    var birthday: LocalDate?,
    var weight: Double?
) {
    constructor(pet: Pet) : this(
        pet.id,
        UserResponse(pet.owner),
        pet.petType,
        pet.bloodType,
        pet.name,
        pet.description,
        pet.birthday,
        pet.weight
    )
}