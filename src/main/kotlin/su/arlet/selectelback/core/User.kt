package su.arlet.selectelback.core

import jakarta.annotation.Generated
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val login : String,
    val email : String,
    val phone : String? = null,
    val surname: String? = null,
    val name : String? = null,
    val lastName: String? = null,
    val passwordHash: String,
    val created: LocalDate,
    val lastActive: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "locationId")
    val location: Location? = null,

    val district: String? = null,
    val avatar: String? = null, // link
    val vkUserName: String? = null,
    val telegramName: String? = null,
    val vkToken: String? = null,
)