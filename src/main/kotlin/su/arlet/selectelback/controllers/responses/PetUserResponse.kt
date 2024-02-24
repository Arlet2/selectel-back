package su.arlet.selectelback.controllers.responses

import su.arlet.selectelback.core.Location
import su.arlet.selectelback.core.User
import java.time.LocalDateTime

class PetUserResponse(
    val id: Long,
    val login: String,
    val email: String? = null,
    var phone: String? = null,
    var surname: String? = null,
    var name: String? = null,
    var middleName: String? = null,
    var lastActive: LocalDateTime,
    var location: Location? = null,
    var avatar: String? = null,
    var vkUserName: String? = null,
    var tgUserName: String? = null
) {
    constructor(user: User) : this(
        user.id,
        user.login,
        if (user.emailVisibility) user.email else null,
        if (user.phoneVisibility) user.phone else null,
        user.surname,
        user.name,
        user.middleName,
        user.lastActive,
        user.location,
        user.avatar,
        user.vkUserName,
        user.tgUserName,
    )
}