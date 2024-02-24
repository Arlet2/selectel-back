package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import su.arlet.selectelback.controllers.filters.RangeFilter
import su.arlet.selectelback.controllers.responses.EntityCreatedResponse
import su.arlet.selectelback.core.*
import su.arlet.selectelback.exceptions.BadEntityException
import su.arlet.selectelback.exceptions.EntityNotFoundException
import su.arlet.selectelback.repos.*
import java.time.LocalDate


@RestController
@RequestMapping("\${api.path}/pets")
@Tag(name = "Pet API")
class PetController @Autowired constructor(
    private val petRepo: PetRepo,
    private val userRepo: UserRepo,
    private val petTypeRepo: PetTypeRepo,
    private val bloodTypeRepo: BloodTypeRepo,
    private val vaccinationsRepo: VaccinationsRepo,
    private val rangeFilter: RangeFilter
) {

    @GetMapping("/")
    @Operation(summary = "Get pets by filters")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun getPets(
        @RequestParam(name = "locationId", required = false) locationId: Long?,
        @RequestParam(name = "city", required = false) city: String?,
        @RequestParam(name = "district", required = false) district: String?,
        @RequestParam(name = "petTypeId", required = false) petTypeId: Long?,
        @RequestParam(name = "petType", required = false) petType: String?,
        @RequestParam(name = "petBreed", required = false) petBreed: String?,
        @RequestParam(name = "bloodTypeId", required = false) bloodTypeId: Long?,
        @RequestParam(name = "bloodType", required = false) bloodType: String?,
    ): List<Pet> {
        return petRepo.findAll().toList().filter {
            if (!rangeFilter.equal(it.owner.location?.id, locationId))
                return@filter false
            if (!rangeFilter.equal(it.owner.location?.city, city))
                return@filter false
            if (it.owner.location?.city == city && !rangeFilter.equal(it.owner.location?.district, district))
                return@filter false

            if (!rangeFilter.equal(it.petType.id, petTypeId))
                return@filter false
            if (!rangeFilter.equal(it.petType.type, petType))
                return@filter false
            if (it.petType.type == petType && !rangeFilter.equal(it.petType.breed, petBreed))
                return@filter false

            if (!rangeFilter.equal(it.bloodType.id, bloodTypeId))
                return@filter false
            if (it.petType.type == petType && !rangeFilter.equal(it.bloodType.bloodType, bloodType))
                return@filter false

            return@filter true
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pet by ID")
    @ApiResponse(responseCode = "200", description = "Success - found pet")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - pet not found", content = [Content()])
    fun getPetById(@PathVariable id: Long): ResponseEntity<Pet> {
        val pet = petRepo.findById(id).orElseThrow{ throw EntityNotFoundException("pet") }
        return ResponseEntity.ok(pet)
    }

    @GetMapping("/{id}/vaccinations")
    @Operation(summary = "Get pet vaccinations")
    @ApiResponse(responseCode = "200", description = "Success - found pet vaccinations")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - pet not found", content = [Content()])
    fun getPetVaccinations(@PathVariable id: Long): ResponseEntity<List<Vaccination>> {
        return if (petRepo.findById(id).isPresent) {
            val vaccinations = vaccinationsRepo.findByPetId(id)
            ResponseEntity.ok(vaccinations)
        } else ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @GetMapping("/types")
    @Operation(summary = "Get all pet types")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun getCities(): List<String> {
        return petTypeRepo.findTypes()
    }

    @GetMapping("/breeds")
    @Operation(summary = "Get all pet types")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun getBreeds(
        @RequestParam(name = "typeName", required = true) typeName: String?,
    ): ResponseEntity<List<PetType>> {
        if (typeName == null) return ResponseEntity(null, HttpStatus.NOT_FOUND)
        val breeds = petTypeRepo.findByType(typeName) ?: throw EntityNotFoundException("type")
        return ResponseEntity.ok(breeds)
    }

    @GetMapping("/blood_types")
    @Operation(summary = "Get all pet types")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun getBloodTypes(
        @RequestParam(name = "typeName", required = true) typeName: String?,
    ): ResponseEntity<List<BloodType>> {
        if (typeName == null) return ResponseEntity(null, HttpStatus.NOT_FOUND)
        val bloodTypes = bloodTypeRepo.findByTypeName(typeName) ?: throw EntityNotFoundException("type")
        return ResponseEntity.ok(bloodTypes)
    }

    @PostMapping("/")
    @Operation(summary = "Create a new pet")
    @ApiResponse(responseCode = "201", description = "Added", content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = EntityCreatedResponse::class)
        )
    ])
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found (incorrect ids)", content = [Content()])
    fun createPet(@RequestBody petRequest: CreatePetRequest, request: HttpServletRequest): ResponseEntity<*> {
        try {
            val createdEntity = petRepo.save(
                Pet(
                    owner = petRequest.ownerId.let {
                        userRepo.findById(it).orElseThrow{ throw EntityNotFoundException("user") }
                    },
                    petType = petRequest.petTypeId.let {
                        petTypeRepo.findById(it).orElseThrow { throw EntityNotFoundException("pet type") }
                    },
                    bloodType = petRequest.bloodTypeId.let {
                        bloodTypeRepo.findById(it).orElseThrow { throw EntityNotFoundException("blood type") }
                    },
                    name = petRequest.name,
                    description = petRequest.description,
                    birthday = petRequest.birthday,
                    weight = petRequest.weight
                )
            )

            return ResponseEntity(EntityCreatedResponse(createdEntity.id), HttpStatus.CREATED)
        } catch (_: IllegalArgumentException) {
            throw BadEntityException("Entity was empty")
        }
    }

    @PostMapping("/{id}/vaccination")
    @Operation(summary = "Create a new pet vaccination")
    @ApiResponse(responseCode = "201", description = "Added", content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = EntityCreatedResponse::class)
        )
    ])
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found", content = [Content()])
    fun createVaccination(
        @PathVariable id: Long, @RequestBody vaccinationRequest: CreateVaccinationRequest
    ): ResponseEntity<*> {
        try {
            val createdEntity = vaccinationsRepo.save(
                Vaccination(
                    pet = petRepo.findById(id).orElseThrow{ throw EntityNotFoundException("pet") },
                    name = vaccinationRequest.name,
                    description = vaccinationRequest.description,
                    vaccinationDate = vaccinationRequest.vaccinationDate
                )
            )

            return ResponseEntity(EntityCreatedResponse(createdEntity.id), HttpStatus.CREATED)
        } catch (_: IllegalArgumentException) {
            throw BadEntityException("Entity was empty")
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update pet info")
    @ApiResponse(responseCode = "200", description = "Success - updated pet")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - pet not found", content = [Content()])
    fun updatePet(@PathVariable id: Long, @RequestBody updatedPet: UpdatePetRequest): ResponseEntity<*> {
        val pet = petRepo.findById(id).orElseThrow{ throw EntityNotFoundException("pet") }
        updatePetFields(pet, updatedPet)
        return ResponseEntity.ok(petRepo.save(pet))
    }

    @PatchMapping("/{id}/vaccination/{vaccinationId}")
    @Operation(summary = "Update pet vaccination info")
    @ApiResponse(responseCode = "200", description = "Success - updated pet vaccination")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - pet not found", content = [Content()])
    fun updatePetVaccination(
        @PathVariable id: Long, @PathVariable vaccinationId: Long,
        @RequestBody updatedVaccination: UpdateVaccinationRequest
    ): ResponseEntity<*> {
        val vaccination = vaccinationsRepo.findById(vaccinationId).orElseThrow{
            throw EntityNotFoundException("vaccination")
        }

        return if (vaccination.pet.id == id) {
            updateVaccinationFields(vaccination, updatedVaccination)
            ResponseEntity.ok(vaccinationsRepo.save(vaccination))
        } else ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete pet")
    @ApiResponse(responseCode = "200", description = "Success - deleted pet")
    @ApiResponse(responseCode = "204", description = "No content", content = [Content()])
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun deletePet(@PathVariable id: Long): ResponseEntity<Void> {
        val pet = petRepo.findById(id)
        return if (pet.isPresent) {
            petRepo.delete(pet.get())
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @DeleteMapping("/{id}/vaccination/{vaccinationId}")
    @Operation(summary = "Delete pet vaccination")
    @ApiResponse(responseCode = "200", description = "Success - deleted pet vaccination")
    @ApiResponse(responseCode = "204", description = "No content", content = [Content()])
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun deleteVaccination(@PathVariable id: Long, @PathVariable vaccinationId: Long): ResponseEntity<Void> {
        val vaccination = vaccinationsRepo.findById(vaccinationId)
        return if (vaccination.isPresent && vaccination.get().pet.id == id) {
            vaccinationsRepo.delete(vaccination.get())
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.noContent().build()
        }
    }

    private fun updatePetFields(pet: Pet, updatedPet: UpdatePetRequest) {
        updatedPet.petType?.let { pet.petType = it }
        updatedPet.bloodType?.let { pet.bloodType = it }
        updatedPet.name?.let { pet.name = it }
        updatedPet.description?.let { pet.description = it }
        updatedPet.birthday?.let { pet.birthday = it }
        updatedPet.weight?.let { pet.weight = it }
    }

    private fun updateVaccinationFields(vaccination: Vaccination, updatedVaccination: UpdateVaccinationRequest) {
        updatedVaccination.name?.let { vaccination.name = it }
        updatedVaccination.description?.let { vaccination.description = it }
        updatedVaccination.vaccinationDate?.let { vaccination.vaccinationDate = it }
    }

    data class CreatePetRequest(
        var ownerId: Long,
        var petTypeId: Long,
        var bloodTypeId: Long,
        var name: String,
        var description: String?,
        var birthday: LocalDate?,
        var weight: Double?
    )

    data class UpdatePetRequest(
        val petType: PetType? = null,
        val bloodType: BloodType? = null,
        val name: String? = null,
        val description: String? = null,
        val birthday: LocalDate? = null,
        val weight: Double? = null
    )

    data class CreateVaccinationRequest(
        var name: String,
        var vaccinationDate: LocalDate,
        var description: String?
    )
    data class UpdateVaccinationRequest(
        var name: String? = null,
        var vaccinationDate: LocalDate? = null,
        var description: String? = null
    )
}