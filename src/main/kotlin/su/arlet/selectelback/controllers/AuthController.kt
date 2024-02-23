package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import su.arlet.selectelback.exceptions.UnauthorizedError
import su.arlet.selectelback.services.AuthService

@RestController
@RequestMapping(value = ["\${api.path}/auth"])
class AuthController @Autowired constructor(
    private val authService: AuthService,
    ){

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
    fun login(@RequestBody(required = true) loginEntity: UserLoginRequest): ResponseEntity<AuthResponse> {
        val (accessToken, refreshToken) = authService.login(loginEntity.login, loginEntity.password)
        return ResponseEntity(AuthResponse(accessToken, refreshToken), HttpStatus.OK)
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
                            ExampleObject(value = "{ field: \"email\" }", name = "Email exists"),
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
    fun register(@RequestBody(required = true) registerEntity: UserRegisterRequest): ResponseEntity<AuthResponse> {
        val (accessToken, refreshToken) = authService.register(
                registerEntity.login,
                registerEntity.email,
                registerEntity.password,
            )
        return ResponseEntity(AuthResponse(accessToken, refreshToken), HttpStatus.OK)
    }

    @Operation(summary = "Logout user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Success logout",
                content = arrayOf()
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
    fun logout(request: HttpServletRequest): ResponseEntity<*> {
        val userID = authService.getUserID(request)
        authService.removeTokens(userID)
        return ResponseEntity(null, HttpStatus.OK)
    }

    @Operation(summary = "Refresh tokens")
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
    @PostMapping(value = ["/refresh"])
    fun refresh(
        request : HttpServletRequest,
        @RequestBody(required = true, description= "refresh token") refreshToken: String,
        ): ResponseEntity<AuthResponse> {
        val (accessToken, newRefreshToken) = try {
            authService.refreshToken(
                authService.getAccessToken(request),
                refreshToken
            )
        } catch (e: RuntimeException) {
            throw UnauthorizedError()
        }

        return ResponseEntity(AuthResponse(accessToken, newRefreshToken), HttpStatus.OK)
    }
}