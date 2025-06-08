package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.EPaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreatePaymentRequest (
    var amount: BigDecimal? = null,
    var paymentDate: LocalDateTime? = null,
    var transactionRef: String?= null,
    var responseCode: String?= null,
    var bankCode: String?= null,
    var status: EPaymentStatus?= null,
    var registrationId: Long? = null
)