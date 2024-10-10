package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payment")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val amount: BigDecimal? = null,
    val paymentDate: LocalDateTime? = null,
    val status: EPaymentStatus? = null
)

enum class EPaymentStatus {
    PENDING, COMPLETED, FAILED, EXPIRED, CANCELLED
}
