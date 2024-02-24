package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import su.arlet.selectelback.core.Location
import su.arlet.selectelback.exceptions.EntityNotFoundException
import su.arlet.selectelback.repos.LocationRepo

@RestController
@RequestMapping("\${api.path}/location")
@Tag(name = "Location API")
class LocationController @Autowired constructor(
    private val locationRepo: LocationRepo,
) {
    @GetMapping("/cities")
    @Operation(summary = "Get all cities")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun getCities(): List<Location> {
        return locationRepo.findByDistrictIsNull()
    }

    @GetMapping("/districts")
    @Operation(summary = "Get all districts by city")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "City not found", content = [Content()])
    fun getDistricts(
        @RequestParam(name = "city", required = true) city: String?
    ): ResponseEntity<List<Location>> {
        if (city == null) return ResponseEntity(null, HttpStatus.NOT_FOUND)
        val districts = locationRepo.findByCity(city) ?: throw EntityNotFoundException("city")
        return ResponseEntity.ok(districts)
    }
}