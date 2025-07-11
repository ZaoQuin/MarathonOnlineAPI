package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.CreatePaymentRequest
import com.university.MarathonOnlineAPI.dto.PaymentDTO

interface PaymentService {
    fun addPayment(newPayment: CreatePaymentRequest): PaymentDTO
    fun deletePaymentById(id: Long)
    fun updatePayment(paymentDTO: PaymentDTO): PaymentDTO
    fun getPayments(): List<PaymentDTO>
    fun getById(id: Long): PaymentDTO
}