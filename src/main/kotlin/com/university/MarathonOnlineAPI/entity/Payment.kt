package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.Date

@Entity
@Table(name = "payment")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val amount: BigDecimal? = null,
    val paymentDate: Date? = null,
    val status: EPaymentStatus? = null
)

enum class EPaymentStatus {
    PENDING, COMPLETED, FAILED, EXPIRED, CANCELLED
}
