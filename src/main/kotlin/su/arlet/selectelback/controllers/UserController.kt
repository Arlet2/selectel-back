package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import su.arlet.selectelback.controllers.filters.RangeFilter
import su.arlet.selectelback.core.*
import su.arlet.selectelback.exceptions.EntityNotFoundException
import su.arlet.selectelback.repos.BloodTypeRepo
import su.arlet.selectelback.repos.LocationRepo
import su.arlet.selectelback.repos.UserRepo
import su.arlet.selectelback.services.AuthService



@RestController
@RequestMapping("\${api.path}/users")
@Tag(name = "Users API")
class UserController @Autowired constructor(
    private val userRepository: UserRepo,
    private val locationRepository: LocationRepo,
    private val authService: AuthService
) {

    @GetMapping("/")
    @Operation(summary = "Get user info")
    @ApiResponse(responseCode = "200", description = "Success - found user")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - user not found", content = [Content()])
    fun getCurrentUsers(request: HttpServletRequest): ResponseEntity<User> {
        val id: Long = authService.getUserID(request)
        val user = userRepository.findById(id).orElseThrow{ throw EntityNotFoundException("user") }
        return ResponseEntity.ok(user)
    }

    @GetMapping("/{login}")
    @Operation(summary = "Get user info")
    @ApiResponse(responseCode = "200", description = "Success - found user")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - user not found", content = [Content()])
    fun getUserByLogin(@PathVariable login: String): ResponseEntity<User> {
        return if (userRepository.existsUserByLogin(login)) {
            val user = userRepository.getUserByLogin(login)
            ResponseEntity.ok(user)
        } else ResponseEntity.notFound().build()
    }

    @PatchMapping("/")
    @Operation(summary = "Update user info")
    @ApiResponse(responseCode = "200", description = "Success - updated user")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Not found - user not found", content = [Content()])
    fun updateUser(@RequestBody updatedUser: UpdateUserRequest, request: HttpServletRequest): ResponseEntity<*> {
        val id: Long = authService.getUserID(request)
        val user = userRepository.findById(id).orElseThrow{ throw EntityNotFoundException("user") }
        updateUserFields(user, updatedUser)
        return ResponseEntity.ok(userRepository.save(user))
    }

    @PostMapping("/changePass")
    @Operation(summary = "Change password")
    @ApiResponse(responseCode = "200", description = "Success - password changed")
    @ApiResponse(responseCode = "401", description = "No token found", content = [Content()])
    @ApiResponse(responseCode = "403", description = "Access Denied", content = [Content()])
    @ApiResponse(responseCode = "409", description = "Conflict", content = [Content()])
    fun changePassword(
        @RequestBody(required = true) updatePass: UpdatePasswordRequest,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val id: Long = authService.getUserID(request)
        val user = userRepository.findById(id).orElseThrow{ throw EntityNotFoundException("user") }

        return if (authService.passwordsEquals(updatePass.oldPassword, user.passwordHash)) {
            user.passwordHash = authService.hashPassword(updatePass.newPassword)
            userRepository.save(user)

            ResponseEntity.ok("Success")
        } else ResponseEntity(null, HttpStatus.CONFLICT)
    }

    private fun updateUserFields(user: User, updatedUser: UpdateUserRequest) {
        updatedUser.phone?.let { user.phone = it }
        updatedUser.surname?.let { user.surname = it }
        updatedUser.name?.let { user.name = it }
        updatedUser.lastName?.let { user.middleName = it }
        updatedUser.locationId?.let {
            user.location = locationRepository.findById(it).orElseThrow { throw EntityNotFoundException("location") }
        }
        updatedUser.vkUserName?.let { user.vkUserName = it }
        updatedUser.tgUserName?.let { user.tgUserName = it }
        updatedUser.emailVisibility?.let { user.emailVisibility = it }
        updatedUser.phoneVisibility?.let { user.phoneVisibility = it }
    }

    data class UpdateUserRequest (
        val phone : String?,
        val surname: String?,
        val name : String?,
        val lastName: String?,
        val locationId: Long?,
        val vkUserName: String?,
        val tgUserName: String?,
        val emailVisibility: Boolean?,
        val phoneVisibility: Boolean?
    )

    data class UpdatePasswordRequest (
        val oldPassword: String,
        val newPassword: String
    )
}