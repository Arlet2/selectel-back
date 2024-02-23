package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @Operation(summary = "Get all deliveries")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successfully getting all deliveries",
                content = arrayOf(
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = String::class))
                    ),
                )
            ),
            ApiResponse(
                responseCode = "401", description = "Where are you token?", content = arrayOf(Content())
            ),
            ApiResponse(
                responseCode = "404", description = "No such delivery was found", content = arrayOf(Content())
            ),
            ApiResponse(
                responseCode = "500", description = "Internal error", content = arrayOf(Content()),
            )
        ]
    )
    @GetMapping("\${api.path}/test")
    @ResponseBody
    fun getDeliveries(
        @RequestParam(name = "transport_number", required = false) transportNumber: String?,
        @RequestParam(name = "delivery_point_id", required = false) deliveryPointID: Int?,
    ): List<Int> {
        return listOf(1, 2, 3)
    }
}