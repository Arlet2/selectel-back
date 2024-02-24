package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import su.arlet.selectelback.controllers.filters.RangeFilter
import su.arlet.selectelback.controllers.responses.EntityCreatedResponse
import su.arlet.selectelback.controllers.responses.PetResponse
import su.arlet.selectelback.core.BloodType
import su.arlet.selectelback.core.Pet
import su.arlet.selectelback.core.PetType
import su.arlet.selectelback.core.Vaccination
import su.arlet.selectelback.exceptions.BadEntityException
import su.arlet.selectelback.exceptions.EntityNotFoundException
import su.arlet.selectelback.repos.*
import su.arlet.selectelback.services.AuthService
import su.arlet.selectelback.services.ImageService
import su.arlet.selectelback.services.staticFilesPath
import java.nio.file.Files
import java.time.LocalDate
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.jvm.optionals.getOrNull


@RestController
@RequestMapping("\${api.path}/pets")
@Tag(name = "Pet API")
class PetController @Autowired constructor(
    private val petRepo: PetRepo,
    private val userRepo: UserRepo,
    private val petTypeRepo: PetTypeRepo,
    private val bloodTypeRepo: BloodTypeRepo,
    private val vaccinationsRepo: VaccinationsRepo,
    private val unavailableDatesRepo: UnavailableDatesRepo,
    private val authService: AuthService,
    private val rangeFilter: RangeFilter,
    private val imageService: ImageService,
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
    ): List<PetResponse> {
        return petRepo.findAll().toList().filter {
            val unavailableDates = unavailableDatesRepo.findById(it.owner.id)

            if (unavailableDates.isPresent) {
                val startDate = unavailableDates.get().startDate
                val endDate = unavailableDates.get().endDate

                if (startDate == null && endDate != null && !endDate.isBefore(LocalDate.now()))
                    return@filter false
                if (startDate != null && endDate == null && !startDate.isAfter(LocalDate.now()))
                    return@filter false
                if (startDate != null && endDate != null &&
                    !startDate.isAfter(LocalDate.now()) && !endDate.isBefore(LocalDate.now())
                ) // in range inclusive
                    return@filter false
            }

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
        }.map { PetResponse(it) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pet by ID")
    @ApiResponse(responseCode = "200", description = "Success - found pet")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - pet not found", content = [Content()])
    fun getPetById(@PathVariable id: Long): ResponseEntity<PetResponse> {
        val pet = petRepo.findById(id).orElseThrow { throw EntityNotFoundException("pet") }
        return ResponseEntity.ok(PetResponse(pet))
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
    fun getCities(): List<PetType> {
        return petTypeRepo.findByBreedIsNull()
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
    @ApiResponse(
        responseCode = "201", description = "Added", content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = EntityCreatedResponse::class)
            )
        ]
    )
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found (incorrect ids)", content = [Content()])
    fun createPet(
        @RequestBody petRequest: CreatePetRequest,
        request: HttpServletRequest
    ): ResponseEntity<EntityCreatedResponse> {
        try {
            val userId = authService.getUserID(request)

            val createdEntity = petRepo.save(
                Pet(
                    owner = userRepo.findById(userId).orElseThrow { throw EntityNotFoundException("user") },
                    petType = petRequest.petTypeId.let {
                        petTypeRepo.findById(it).orElseThrow { throw EntityNotFoundException("pet type") }
                    },
                    bloodType = petRequest.bloodTypeId.let {
                        bloodTypeRepo.findById(it).orElseThrow { throw EntityNotFoundException("blood type") }
                    },
                    name = petRequest.name,
                    description = petRequest.description,
                    birthday = petRequest.birthday,
                    weight = petRequest.weight,
                )
            )

            return ResponseEntity(EntityCreatedResponse(createdEntity.id), HttpStatus.CREATED)
        } catch (e: IllegalArgumentException) {
            throw BadEntityException("Entity was empty: $e")
        }
    }

    @Operation(summary = "Upload avatar")
    @ApiResponse(responseCode = "200", description = "Success - avatar uploaded")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - user not found", content = [Content()])
    @PostMapping("/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAvatarFile(
        request: HttpServletRequest,
        @RequestParam("file") file: MultipartFile,
    ): Any? {
        val pet = petRepo.findById(0).getOrNull() ?: throw EntityNotFoundException("pet")

        val resJsonData = JSONObject()
        try {
            if (file.isEmpty) {
                println("Empty")
            }

            val lastDotIndex = file.originalFilename?.lastIndexOf('.') ?: 0
            val extension = if (lastDotIndex > 0) {
                file.originalFilename?.substring(lastDotIndex)
            } else {
                null
            }

            if (extension == null) {
                return ResponseEntity("no extension on file", HttpStatus.BAD_REQUEST)
            }

            val filename = "pet"+imageService.hashFilename(file.name) + extension
            val path = Path( staticFilesPath.pathString, filename).toAbsolutePath()

            // todo: if file exists
            Files.copy(file.inputStream, path)

            resJsonData.put("status", 200)
            resJsonData.put("message", "Success!")
            resJsonData.put("link", path.toAbsolutePath())

            pet.avatar = "https://petdonor.ru/avatar/$filename"
            petRepo.save(pet)
        } catch (e: Exception) {
            println(e)
            resJsonData.put("status", 400)
            resJsonData.put("message", "Upload Image Error!")
            resJsonData.put("data", "")
        }
        return resJsonData.toString()
    }

    @PostMapping("/{id}/vaccination")
    @Operation(summary = "Create a new pet vaccination")
    @ApiResponse(
        responseCode = "201", description = "Added", content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = EntityCreatedResponse::class)
            )
        ]
    )
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found", content = [Content()])
    fun createVaccination(
        @PathVariable id: Long, @RequestBody vaccinationRequest: CreateVaccinationRequest
    ): ResponseEntity<EntityCreatedResponse> {
        try {
            val createdEntity = vaccinationsRepo.save(
                Vaccination(
                    pet = petRepo.findById(id).orElseThrow { throw EntityNotFoundException("pet") },
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
    fun updatePet(@PathVariable id: Long, @RequestBody updatedPet: UpdatePetRequest): ResponseEntity<PetResponse> {
        val pet = petRepo.findById(id).orElseThrow { throw EntityNotFoundException("pet") }
        updatePetFields(pet, updatedPet)
        petRepo.save(pet)
        return ResponseEntity.ok(PetResponse(pet))
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
        val vaccination = vaccinationsRepo.findById(vaccinationId).orElseThrow {
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
        updatedPet.petTypeId?.let {
            pet.petType = petTypeRepo.findById(it).orElseThrow { throw EntityNotFoundException("petType") }
        }
        updatedPet.bloodTypeId?.let {
            pet.bloodType = bloodTypeRepo.findById(it).orElseThrow { throw EntityNotFoundException("bloodType") }
        }
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
        var petTypeId: Long,
        var bloodTypeId: Long,
        var name: String,
        var description: String?,
        var birthday: LocalDate?,
        var weight: Double?
    )

    data class UpdatePetRequest(
        val petTypeId: Long? = null,
        val bloodTypeId: Long? = null,
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