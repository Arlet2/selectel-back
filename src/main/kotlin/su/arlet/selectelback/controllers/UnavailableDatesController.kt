package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import su.arlet.selectelback.core.UnavailableDates
import su.arlet.selectelback.exceptions.BadEntityException
import su.arlet.selectelback.exceptions.EntityNotFoundException
import su.arlet.selectelback.repos.UnavailableDatesRepo
import su.arlet.selectelback.services.AuthService
import java.time.LocalDate

@RestController
@RequestMapping("\${api.path}/users/unavailable_dates")
class UnavailableDatesController @Autowired constructor(
    val authService: AuthService,
    val unavailableDatesRepo: UnavailableDatesRepo,
) {

    data class UnavailableDatesRequest(
        val startDate: LocalDate?,
        val endDate: LocalDate?,
    )

    data class UnavailableDatesUpdateRequest(
        val startDate: LocalDate?,
        val endDate: LocalDate?,
    )

    @GetMapping("/")
    @Operation(summary = "Get unavailable dates")
    @ApiResponse(responseCode = "200", description = "OK", content = [
        Content(schema = Schema(implementation = UnavailableDates::class))],
        )
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun getUnavailableDates(
        request: HttpServletRequest,
    ): ResponseEntity<*> {
        val userID = authService.getUserID(request)

        val unavailableDates = unavailableDatesRepo.findById(userID)
        if (unavailableDates.isPresent)
            return ResponseEntity(unavailableDates, HttpStatus.OK)

        return ResponseEntity(
            UnavailableDates(
                userID = userID,
                startDate = null,
                endDate = null,
            ), HttpStatus.OK
        )
    }

    @PostMapping("/")
    @Operation(summary = "Create a new unavailable dates")
    @ApiResponse(
        responseCode = "201", description = "Added", content = [
            Content(
                mediaType = "application/json",
                schema = Schema()
            )
        ]
    )
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found (incorrect ids)", content = [Content()])
    fun createUnavailableDates(
        request: HttpServletRequest,
        @RequestBody unavailableDatesRequest: UnavailableDatesRequest
    ): ResponseEntity<*> {
        val userID = authService.getUserID(request)
        try {
            unavailableDatesRepo.save(
                UnavailableDates(
                    userID = userID,
                    startDate = unavailableDatesRequest.startDate,
                    endDate = unavailableDatesRequest.endDate,
                )
            )

            return ResponseEntity(null, HttpStatus.CREATED)
        } catch (_: IllegalArgumentException) {
            throw BadEntityException("Entity was empty")
        }
    }

    @PatchMapping("/")
    @Operation(summary = "Update unavailable dates")
    @ApiResponse(responseCode = "200", description = "Success - updated unavailable dates")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - donor request not found", content = [Content()])
    fun updateUnavailableDates(
        request: HttpServletRequest,
        @RequestBody unavailableDatesUpdateRequest: UnavailableDatesUpdateRequest,
    ): ResponseEntity<*> {
        val userID = authService.getUserID(request)

        val unavailableDates =
            unavailableDatesRepo.findById(userID).orElseThrow { throw EntityNotFoundException("unavailable dates") }
        updateUnavailableDates(unavailableDates, unavailableDatesUpdateRequest)
        return ResponseEntity.ok(unavailableDatesRepo.save(unavailableDates))
    }

    @DeleteMapping("/")
    @Operation(summary = "Delete unavailable")
    @ApiResponse(responseCode = "200", description = "Success - deleted unavailable dates")
    @ApiResponse(responseCode = "204", description = "No content", content = [Content()])
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun deleteUnavailableDates(
        request: HttpServletRequest,
    ): ResponseEntity<Void> {
        val userID = authService.getUserID(request)

        val unavailableDates = unavailableDatesRepo.findById(userID)
        return if (unavailableDates.isPresent) {
            unavailableDatesRepo.delete(unavailableDates.get())
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.noContent().build()
        }
    }

    private fun updateUnavailableDates(
        unavailableDates: UnavailableDates,
        updateRequest: UnavailableDatesUpdateRequest,
    ) {
        updateRequest.startDate?.let { unavailableDates.startDate = it }
        updateRequest.endDate?.let { unavailableDates.endDate = it }
    }

}