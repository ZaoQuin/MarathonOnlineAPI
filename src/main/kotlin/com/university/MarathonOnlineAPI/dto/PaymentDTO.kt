package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.EPaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentDTO (
    val id: Long? = null,
    val amount: BigDecimal? = null,
    val paymentDate: LocalDateTime? = null,
    val status: EPaymentStatus? = null,
)