package com.university.MarathonOnlineAPI.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
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
    val transactionRef: String? = null,
    val responseCode: String?= null,
    val bankCode: String?= null,
    var status: EPaymentStatus?= null
)

enum class EPaymentStatus {
    SUCCESS, FAILED, PENDING
}