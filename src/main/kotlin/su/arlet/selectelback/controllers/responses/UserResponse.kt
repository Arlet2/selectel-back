package su.arlet.selectelback.controllers.responses

import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import su.arlet.selectelback.core.Location
import su.arlet.selectelback.core.User
import java.time.LocalDate
import java.time.LocalDateTime

class UserResponse(
    val id: Long,
    val login : String,
    val email : String,
    var phone : String? = null,
    var surname: String? = null,
    var name : String? = null,
    var middleName: String? = null,
    var lastActive: LocalDateTime,
    var location: Location? = null,
    var avatar: String? = null,
    var vkUserName: String? = null,
    var tgUserName: String? = null,
    var emailVisibility: Boolean? = null,
    var phoneVisibility: Boolean? = null
) {
    constructor(user: User) : this(
        user.id,
        user.login,
        user.email,
        user.phone,
        user.surname,
        user.name,
        user.middleName,
        user.lastActive,
        user.location,
        user.avatar,
        user.vkUserName,
        user.tgUserName,
        user.emailVisibility,
        user.phoneVisibility
    )
}