package su.arlet.selectelback.controllers.responses

import com.fasterxml.jackson.annotation.JsonFormat
import su.arlet.selectelback.core.BloodType
import su.arlet.selectelback.core.Pet
import su.arlet.selectelback.core.PetType
import java.time.LocalDate

class PetResponse(
    val id: Long,
    var owner: PetUserResponse,
    var petType: PetType,
    var bloodType: BloodType,
    var name: String,
    var description: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var birthday: LocalDate?,
    var weight: Double?,
    var avatar: String?,
) {
    constructor(pet: Pet) : this(
        pet.id,
        PetUserResponse(pet.owner),
        pet.petType,
        pet.bloodType,
        pet.name,
        pet.description,
        pet.birthday,
        pet.weight,
        pet.avatar,
    )
}