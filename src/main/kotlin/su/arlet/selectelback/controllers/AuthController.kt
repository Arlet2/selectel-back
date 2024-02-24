package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import lombok.EqualsAndHashCode
import lombok.Getter
import lombok.Setter
import org.json.JSONException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import su.arlet.selectelback.core.Location
import su.arlet.selectelback.core.User
import su.arlet.selectelback.exceptions.IncorrectPasswordError
import su.arlet.selectelback.exceptions.UnauthorizedError
import su.arlet.selectelback.exceptions.UserExistsError
import su.arlet.selectelback.exceptions.UserNotFoundError
import su.arlet.selectelback.repos.LocationRepo
import su.arlet.selectelback.repos.UserRepo
import su.arlet.selectelback.services.AuthService
import su.arlet.selectelback.services.PostRequestService
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping(value = ["\${api.path}/auth"])
@Tag(name = "Auth API")
class AuthController @Autowired constructor(
    private val userRepository: UserRepo,
    private val locationRepo: LocationRepo,
    private val authService: AuthService,
    private val postRequestService: PostRequestService
) {
    data class UserLoginRequest(
        val login: String,
        val password: String,
    )

    data class AuthResponse(
        val accessToken: String,
        val refreshToken: String,
    )

    data class Error(val error: String)

    @Setter
    @Getter
    @EqualsAndHashCode
    class UserRegisterRequest(
        var login: String,
        var password: String,
        var email: String,
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
    fun login(@RequestBody(required = true) loginEntity: UserLoginRequest): ResponseEntity<*> {
        val (accessToken, refreshToken) = try {
            authService.login(loginEntity.login, loginEntity.password)
        } catch (e: IncorrectPasswordError) {
            return ResponseEntity(Error(error = "Неправильный логин или пароль"), HttpStatus.CONFLICT)
        } catch (e: UserNotFoundError) {
            return ResponseEntity(Error(error = "Неправильный логин или пароль"), HttpStatus.CONFLICT)
        }
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

                        schema = Schema(implementation = AuthResponse::class)
                    ),
                )
            ),
            ApiResponse(
                responseCode = "409", description = "Login/email exists", content = arrayOf(
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = UserExistsError::class),
                    ),
                )
            ),
            ApiResponse(
                responseCode = "500", description = "Internal error", content = arrayOf(Content()),
            )
        ]
    )
    @PostMapping(value = ["/register"])
    fun register(@RequestBody registerEntity: UserRegisterRequest): ResponseEntity<*> {
        println(registerEntity.toString())

        val (accessToken, refreshToken) = try {
            authService.register(
                registerEntity.login,
                registerEntity.email,
                registerEntity.password,
            )
        } catch (e: UserExistsError) {
            data class Error(val error: String)
            return ResponseEntity(Error(error = "Email или логин уже существует"), HttpStatus.CONFLICT)
        }
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
                responseCode = "401", description = "Unauthorized", content = arrayOf(Content())
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

                        schema = Schema(implementation = AuthResponse::class)
                    ),
                )
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized", content = arrayOf(Content())
            ),
            ApiResponse(
                responseCode = "500", description = "Internal error", content = arrayOf(Content()),
            )
        ]
    )
    @PostMapping(value = ["/refresh"])
    fun refresh(
        request: HttpServletRequest,
        @RequestBody(required = true) refreshToken: String,
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

    @PostMapping("/vk_login")
    @Operation(summary = "Login user by VK")
    @ApiResponse(responseCode = "200", description = "Success - user authorized")
    @ApiResponse(responseCode = "204", description = "No content", content = [Content()])
    @ApiResponse(responseCode = "401", description = "Wrong token given", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    fun loginVk(@RequestBody(required = true) vkAuthRequest: VkAuthRequest): ResponseEntity<*> {
        val response = try {
            postRequestService.approveVkLogin(
                token = vkAuthRequest.token, uuid = vkAuthRequest.uuid
            )
        } catch (_: IllegalStateException) {
            return ResponseEntity("Invalid token", HttpStatus.UNAUTHORIZED)
        }

        val vkUserId: String = response["user_id"].toString()

        val user: User = if (userRepository.existsUserByVkUserId(vkUserId)) {
            userRepository.getUserByVkUserId(vkUserId)
        } else {
            val userInfo = try {
                postRequestService.getUserInfo(
                    userToken = response["access_token"].toString(), vkUserId = vkUserId
                )
            } catch (_: IllegalStateException) { null }

            println("Get user Info: $userInfo")

            val location: Location? = userInfo?.let {
                try {
                    val city = it.getJSONObject("city").getString("title")

                    if (locationRepo.existsLocationByCity(city))
                        locationRepo.getLocationByCityAndDistrictIsNull(city)
                    else null
                } catch (_: JSONException) { null }
            }

            val userLogin = "vk_${vkUserId}"
            val user = User(
                login = userLogin,
                name = userInfo?.getString("first_name"),
                surname = userInfo?.getString("last_name"),
                location = location,
                created = LocalDate.now(),
                lastActive = LocalDateTime.now(),
                vkUserId = vkUserId,
                isPasswordSet = false
            )
            userRepository.save(user)
        }

        val (accessToken, refreshToken) = authService.createTokenById(user.id)

        return ResponseEntity(VkAuthResponse(user.login, accessToken, refreshToken), HttpStatus.OK)
    }

    data class VkAuthRequest(
        val token: String,
        val uuid: String
    )

    data class VkAuthResponse(
        val login: String,
        val accessToken: String,
        val refreshToken: String,
    )
}