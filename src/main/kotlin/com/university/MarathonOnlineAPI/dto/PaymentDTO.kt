package com.university.MarathonOnlineAPI.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentDTO (
    var id: Long? = null,
    var amount: BigDecimal? = null,
    var paymentDate: LocalDateTime? = null
)