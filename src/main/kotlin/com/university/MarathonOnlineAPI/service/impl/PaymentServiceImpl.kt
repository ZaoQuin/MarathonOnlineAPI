package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.CreatePaymentRequest
import com.university.MarathonOnlineAPI.dto.PaymentDTO
import com.university.MarathonOnlineAPI.entity.ERegistrationStatus
import com.university.MarathonOnlineAPI.entity.Payment
import com.university.MarathonOnlineAPI.exception.PaymentException
import com.university.MarathonOnlineAPI.mapper.PaymentMapper
import com.university.MarathonOnlineAPI.repos.PaymentRepository
import com.university.MarathonOnlineAPI.repos.RegistrationRepository
import com.university.MarathonOnlineAPI.service.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val paymentMapper: PaymentMapper,
    private val registrationRepository: RegistrationRepository
) : PaymentService {

    private val logger = LoggerFactory.getLogger(PaymentServiceImpl::class.java)

    override fun addPayment(newPayment: CreatePaymentRequest): PaymentDTO {
        logger.info("Received PaymentDTO: $newPayment")
        try {
            val payment = Payment().apply {
                amount = newPayment.amount
                paymentDate = LocalDateTime.now()
            }

            paymentRepository.save(payment)

            val registration = registrationRepository.findById(newPayment.registration?.id ?: throw IllegalArgumentException("Registration ID is required"))
                .orElseThrow { IllegalArgumentException("Registration not found") }

            registration.payment = payment
            registration.status = ERegistrationStatus.ACTIVE
            registrationRepository.save(registration)

            return paymentMapper.toDto(payment)
        } catch (e: Exception){
            throw PaymentException("Error adding payment: ${e.message}")
        }
    }

    override fun deletePaymentById(id: Long) {
        try {
            paymentRepository.deleteById(id)
            logger.info("Payment with ID $id deleted successfully")
        } catch (e: DataAccessException) {
            logger.error("Error deleting payment with ID $id: ${e.message}")
            throw PaymentException("Database error occurred while deleting payment: ${e.message}")
        }
    }

    override fun updatePayment(paymentDTO: PaymentDTO): PaymentDTO {
        return try {
            val paymentEntity = paymentMapper.toEntity(paymentDTO)
            val updatedPayment = paymentRepository.save(paymentEntity)
            paymentMapper.toDto(updatedPayment)
        } catch (e: DataAccessException) {
            logger.error("Error updating payment: ${e.message}")
            throw PaymentException("Database error occurred while updating payment: ${e.message}")
        }
    }

    override fun getPayments(): List<PaymentDTO> {
        return try {
            val payments = paymentRepository.findAll()

            logger.info("Retrieved payments from database: $payments")

            val paymentDTOs = payments.map { payment ->
                PaymentDTO(
                    id = payment.id,
                    amount = payment.amount,
                    paymentDate = payment.paymentDate,
                )
            }

            logger.info("Mapped PaymentDTOs: $paymentDTOs")

            paymentDTOs
        } catch (e: DataAccessException) {
            logger.error("Error retrieving payments: ${e.message}")
            throw PaymentException("Database error occurred while retrieving payments: ${e.message}")
        }
    }

    override fun getById(id: Long): PaymentDTO {
        return try {
            val payment = paymentRepository.findById(id)
                .orElseThrow { PaymentException("Payment with ID $id not found") }
            paymentMapper.toDto(payment)
        } catch (e: DataAccessException) {
            logger.error("Error retrieving payment with ID $id: ${e.message}")
            throw PaymentException("Database error occurred while retrieving payment: ${e.message}")
        }
    }
}
