package su.arlet.selectelback.core

import jakarta.persistence.*

@Entity
@Table(name = "pet_types")
data class PetType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val type: String,
    val breed: String?
)