package su.arlet.selectelback.core

import jakarta.persistence.*


@Entity
@Table(name = "blood_types")
data class BloodType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val typeName: String,
    val bloodType: String
)