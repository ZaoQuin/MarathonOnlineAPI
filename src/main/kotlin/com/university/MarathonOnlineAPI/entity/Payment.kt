package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payment")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var amount: BigDecimal? = null,
    var paymentDate: LocalDateTime? = null,
    var status: EPaymentStatus? = null
)

enum class EPaymentStatus {
    PENDING, COMPLETED, FAILED, EXPIRED, CANCELLED
}
