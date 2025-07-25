package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.EPaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentDTO (
    var id: Long? = null,
    var amount: BigDecimal? = null,
    var paymentDate: LocalDateTime? = null,
    val transactionRef: String?= null,
    val responseCode: String?= null,
    val bankCode: String?= null,
    var status: EPaymentStatus?= null
)