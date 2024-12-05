package com.university.MarathonOnlineAPI.dto

import java.math.BigDecimal

data class CreatePaymentRequest (
    val amount: BigDecimal? = null,
    val registration: RegistrationDTO? = null
)