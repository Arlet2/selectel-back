package su.arlet.selectelback.core

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,
    val login: String,
    var email: String? = null,
    var phone: String? = null,
    var surname: String? = null,
    var name: String? = null,
    var middleName: String? = null,
    var passwordHash: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val created: LocalDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var lastActive: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "locationId")
    var location: Location? = null,

    var avatar: String? = "https://petdonor.ru/avatar/default-avatar-user.jpg", // link
    var vkUserName: String? = null,
    var tgUserName: String? = null,
    val vkUserId: String? = null,

    var isPasswordSet: Boolean = true,
    var emailVisibility: Boolean = true,
    var phoneVisibility: Boolean = true
)