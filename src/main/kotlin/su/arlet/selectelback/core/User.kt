package su.arlet.selectelback.core

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User (
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,
    val login : String,
    val email : String,
    var phone : String? = null,
    var surname: String? = null,
    var name : String? = null,
    var middleName: String? = null,
    var passwordHash: String,
    val created: LocalDate,
    var lastActive: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "locationId")
    var location: Location? = null,

    var avatar: String? = null, // link
    var vkUserName: String? = null,
    var tgUserName: String? = null,
    val vkUserId: String? = null,

    var emailVisibility: Boolean? = true,
    var phoneVisibility: Boolean? = true
)