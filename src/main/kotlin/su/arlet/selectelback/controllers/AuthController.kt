package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["\${api.path}/auth"])
class AuthController {

    data class UserLoginRequest(
        val login: String,
        val password: String,
    )

    data class AuthResponse(
        val accessToken: String,
        val refreshToken: String,
    )

    data class UserRegisterRequest(
        val login: String,
        val password: String,
        val email: String,
    )

    data class UserExistsError(
        val field: String
    )

    @Operation(summary = "Login user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Success",
                content = arrayOf(
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AuthResponse::class)
                    ),
                )
            ),
            ApiResponse(
                responseCode = "500", description = "Internal error", content = arrayOf(Content()),
            )
        ]
    )
    @PostMapping(value = ["/login"])
    fun login(loginEntity: UserLoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity(AuthResponse("", ""), HttpStatus.OK)
    }

    @Operation(summary = "Register user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Success",
                content = arrayOf(
                    Content(
                        mediaType = "application/json",

                        schema =Schema(implementation = AuthResponse::class)
                    ),
                )
            ),
            ApiResponse(
                responseCode = "409", description = "Login/email exists", content = arrayOf(
                    Content(
                        mediaType = "application/json",
                                schema =Schema(implementation = UserExistsError::class),
                        examples = arrayOf(
                            ExampleObject(value = "{ field: d\"email\" }", name = "Email exists"),
                            ExampleObject(value = "{ field: \"login\" }", name = "Login exists")
                            )
                    ),
                )
            ),
            ApiResponse(
                responseCode = "500", description = "Internal error", content = arrayOf(Content()),
            )
        ]
    )
    @PostMapping(value = ["/register"])
    fun register(registerEntity: UserRegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity(AuthResponse("", ""), HttpStatus.OK)
    }

    @Operation(summary = "Logout user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Success logout",
                content = arrayOf(
                    Content(
                        mediaType = "application/json",

                        schema =Schema(implementation = AuthResponse::class)
                    ),
                )
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized", content = arrayOf()
            ),
            ApiResponse(
                responseCode = "500", description = "Internal error", content = arrayOf(Content()),
            )
        ]
    )
    @PostMapping(value = ["/logout"])
    fun logout(): ResponseEntity<*> {
        return ResponseEntity(null, HttpStatus.OK)
    }
}