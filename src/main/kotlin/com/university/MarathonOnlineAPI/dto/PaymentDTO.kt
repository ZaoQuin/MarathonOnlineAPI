package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.EPaymentStatus
import java.math.BigDecimal
import java.util.*

data class PaymentDTO (
    val id: Long? = null,
    val amount: BigDecimal? = null,
    val paymentDate: Date? = null,
    val status: EPaymentStatus? = null,
)