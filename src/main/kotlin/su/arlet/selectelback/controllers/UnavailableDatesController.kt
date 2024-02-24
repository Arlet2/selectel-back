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
    @ApiResponse(
        responseCode = "200", description = "OK",
        content = [
            Content(schema = Schema(implementation = UnavailableDates::class))],
    )
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun getUnavailableDates(
        request: HttpServletRequest,
    ): ResponseEntity<*> {
        val userID = authService.getUserID(request)

        val unavailableDates = unavailableDatesRepo.findById(userID)
        if (unavailableDates.isPresent) {
            val endDate = unavailableDates.get().endDate

            if (endDate != null && endDate.isBefore(LocalDate.now())) {
                return ResponseEntity(
                    UnavailableDates(
                        userID = userID,
                        startDate = null,
                        endDate = null,
                    ), HttpStatus.OK
                )
            }

            return ResponseEntity(unavailableDates.get(), HttpStatus.OK)
        }

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
    ): ResponseEntity<UnavailableDates> {
        val userID = authService.getUserID(request)
        try {
            val unavailableDates = unavailableDatesRepo.save(
                UnavailableDates(
                    userID = userID,
                    startDate = unavailableDatesRequest.startDate,
                    endDate = unavailableDatesRequest.endDate,
                )
            )

            return ResponseEntity(unavailableDates, HttpStatus.CREATED)
        } catch (_: IllegalArgumentException) {
            throw BadEntityException("Entity was empty")
        }
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

}