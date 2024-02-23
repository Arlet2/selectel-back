package su.arlet.selectelback.core

import jakarta.persistence.*

@Entity
@Table(name = "locations")
data class Location(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val city: String,
    val district: String?
)