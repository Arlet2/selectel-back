package su.arlet.selectelback.controllers

import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
    fun getUserById(request: HttpServletRequest): ResponseEntity<User> {
        val id: Long = authService.getUserID(request)
        val user = userRepository.findById(id).orElseThrow{ throw EntityNotFoundException("user") }
        return ResponseEntity.ok(user)
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

    private fun updateUserFields(user: User, updatedUser: UpdateUserRequest) {
        updatedUser.phone?.let { user.phone = it }
        updatedUser.surname?.let { user.surname = it }
        updatedUser.name?.let { user.name = it }
        updatedUser.lastName?.let { user.middleName = it }
        updatedUser.password?.let { user.passwordHash = authService.hashPassword(it) }
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
        val password: String?,
        val locationId: Long?,
        val vkUserName: String?,
        val tgUserName: String?,
        val emailVisibility: Boolean?,
        val phoneVisibility: Boolean?
    )
}