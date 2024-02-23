package su.arlet.selectelback.core

import jakarta.persistence.*

@Entity
@Table(name="tokens")
class Tokens (
    @Id
    val userID : Long,

    val accessToken: String,
    val refreshToken: String,
)