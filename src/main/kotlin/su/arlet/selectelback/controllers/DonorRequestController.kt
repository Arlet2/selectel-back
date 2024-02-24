package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import su.arlet.selectelback.controllers.filters.RangeFilter
import su.arlet.selectelback.controllers.responses.EntityCreatedResponse
import su.arlet.selectelback.core.BloodType
import su.arlet.selectelback.core.DonorRequest
import su.arlet.selectelback.core.Location
import su.arlet.selectelback.core.PetType
import su.arlet.selectelback.exceptions.BadEntityException
import su.arlet.selectelback.exceptions.EntityNotFoundException
import su.arlet.selectelback.repos.BloodTypeRepo
import su.arlet.selectelback.repos.DonorRequestRepo
import su.arlet.selectelback.repos.PetTypeRepo
import su.arlet.selectelback.repos.UserRepo
import su.arlet.selectelback.services.AuthService
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping("\${api.path}/donor_requests")
@Tag(name = "Donor requests API")
class DonorRequestController @Autowired constructor(
    private val donorRequestRepo: DonorRequestRepo,
    private val rangeFilter: RangeFilter,
    private val authService: AuthService,
    private val userRepo: UserRepo,
    private val petTypeRepo: PetTypeRepo,
    private val bloodTypeRepo: BloodTypeRepo,
) {

    data class DonorRequestPresenter(
        var id: Long,
        var location: Location?,
        var description: String,
        var vetAddress: String,
        var petType: PetType,
        var bloodType: BloodType,
        var bloodAmountMl: Double,
        var availableUntil: LocalDate?,
    )

    @GetMapping("/")
    @Operation(summary = "Get all donor requests")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun getDonorRequests(
        request: HttpServletRequest,
        @RequestParam(name = "me", required = false) isMyRequests: Boolean?,
        @RequestParam(name = "date_before", required = false) dateBefore: LocalDate?,
        @RequestParam(name = "date_after", required = false) dateAfter: LocalDate?,
        @RequestParam(name = "location_id", required = false) locationID: Long?,
        @RequestParam(name = "pet_type_id", required = false) petTypeID: Long?,
        @RequestParam(name = "blood_type_id", required = false) bloodTypeID: Long?,
    ): ResponseEntity<*> {
        val userID = authService.getUserID(request)

        if (dateBefore != null && dateAfter == null || dateBefore == null && dateAfter != null) {
            return ResponseEntity("only range is possible", HttpStatus.BAD_REQUEST)
        }

        val donorRequests = donorRequestRepo.findAll().toList().filter {
            if (isMyRequests != null) {
                if (isMyRequests && it.user.id != userID) { // not null and true meaning
                    return@filter false
                } else if (!isMyRequests && it.user.id == userID) {
                    return@filter false
                }
            }

            if (dateBefore != null) {
                if (!dateBefore.isBefore(it.availableUntil)) {
                    return@filter false
                }
            }

            if (dateAfter != null) {
                if (!dateAfter.isAfter(it.availableUntil)) {
                    return@filter false
                }
            }

            if (!rangeFilter.equal(it.user.location?.id, locationID))
                return@filter false

            if (!rangeFilter.equal(it.petType.id, petTypeID))
                return@filter false

            if (!rangeFilter.equal(it.bloodType.id, bloodTypeID))
                return@filter false

            return@filter true
        }.map {
            return@map DonorRequestPresenter(
                id = it.id,
                location = it.user.location,
                description = it.description,
                vetAddress = it.vetAddress,
                petType = it.petType,
                bloodType = it.bloodType,
                bloodAmountMl = it.bloodAmountMl,
                availableUntil = it.availableUntil,
            )
        }

        return ResponseEntity(donorRequests, HttpStatus.OK)
    }

    @PostMapping("/")
    @Operation(summary = "Create a new donor request")
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
    fun createDonorRequest(
        request: HttpServletRequest,
        @RequestBody createDonorRequest: CreateDonorRequest
    ): ResponseEntity<*> {
        val userID = authService.getUserID(request)
        try {
            val createdEntity = donorRequestRepo.save(
                DonorRequest(
                    user = userRepo.findById(userID).getOrNull() ?: return ResponseEntity(
                        "user not found",
                        HttpStatus.NOT_FOUND
                    ),
                    description = createDonorRequest.description ?: return ResponseEntity(
                        "description missing",
                        HttpStatus.BAD_REQUEST
                    ),
                    vetAddress = createDonorRequest.vetAddress ?: return ResponseEntity(
                        "vet address missing",
                        HttpStatus.BAD_REQUEST
                    ),
                    petType = petTypeRepo.findById(createDonorRequest.petTypeID).getOrNull()
                        ?: return ResponseEntity("pet type ID missing", HttpStatus.BAD_REQUEST),
                    bloodType = bloodTypeRepo.findById(createDonorRequest.bloodTypeID).getOrNull()
                        ?: return ResponseEntity("blood type ID missing", HttpStatus.NOT_FOUND),
                    bloodAmountMl = createDonorRequest.bloodAmountMl ?: return ResponseEntity(
                        "blood amount ml missing",
                        HttpStatus.BAD_REQUEST
                    ),
                    availableUntil = createDonorRequest.availableUntil
                        ?: return ResponseEntity("available until missing", HttpStatus.BAD_REQUEST),
                )
            )

            return ResponseEntity(EntityCreatedResponse(createdEntity.id), HttpStatus.CREATED)
        } catch (_: IllegalArgumentException) {
            throw BadEntityException("Entity was empty")
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get donor request by id")
    @ApiResponse(responseCode = "200", description = "Success - found pet")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - donor request not found", content = [Content()])
    fun getDonorRequestByID(@PathVariable id: Long): ResponseEntity<DonorRequest> {
        val donorRequest = donorRequestRepo.findById(id).orElseThrow { throw EntityNotFoundException("donor request") }
        return ResponseEntity.ok(donorRequest)
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update donor request")
    @ApiResponse(responseCode = "200", description = "Success - updated donor request")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - donor request not found", content = [Content()])
    fun updateDonorRequest(
        @PathVariable id: Long,
        @RequestBody updateDonorRequest: UpdateDonorRequest
    ): ResponseEntity<*> {
        val donorRequest = donorRequestRepo.findById(id).orElseThrow { throw EntityNotFoundException("donor request") }
        updateDonorRequest(donorRequest, updateDonorRequest)
        return ResponseEntity.ok(donorRequestRepo.save(donorRequest))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete donor request")
    @ApiResponse(responseCode = "200", description = "Success - deleted donor request")
    @ApiResponse(responseCode = "204", description = "No content", content = [Content()])
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun deleteDonorRequest(@PathVariable id: Long): ResponseEntity<Void> {
        val donorRequest = donorRequestRepo.findById(id)
        return if (donorRequest.isPresent) {
            donorRequestRepo.delete(donorRequest.get())
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.noContent().build()
        }
    }

    private fun updateDonorRequest(donorRequest: DonorRequest, updateDonorRequest: UpdateDonorRequest) {
        updateDonorRequest.description?.let { donorRequest.description = it }
        updateDonorRequest.vetAddress?.let { donorRequest.vetAddress = it }
        updateDonorRequest.bloodType?.let { donorRequest.bloodType = it }
        updateDonorRequest.bloodAmountMl?.let { donorRequest.bloodAmountMl = it }
        updateDonorRequest.availableUntil?.let { donorRequest.availableUntil = it }
    }

    data class CreateDonorRequest(
        var description: String?,
        var vetAddress: String?,
        var petTypeID: Long,
        var bloodTypeID: Long,
        var bloodAmountMl: Double?,
        var availableUntil: LocalDate?,
    )

    data class UpdateDonorRequest(
        var description: String? = null,
        var vetAddress: String? = null,
        var bloodType: BloodType? = null,
        var bloodAmountMl: Double? = null,
        var availableUntil: LocalDate? = null,
    )
}