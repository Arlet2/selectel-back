package su.arlet.selectelback.core

import jakarta.annotation.Generated
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    val login : String,
    val email : String,
    val phone : String,
    val surname: String,
    val name : String,
    val lastName: String,
    val passwordHash: String,
    val created: LocalDate,
    val lastActive: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "locationId")
    val location: Location?,

    val district: String,
    val avatar: String, // link
    val vkUserName: String,
    val telegramName: String,
    val vkToken: String?,
)