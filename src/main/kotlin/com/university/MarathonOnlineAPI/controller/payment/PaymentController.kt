package com.university.MarathonOnlineAPI.controller.payment

import com.university.MarathonOnlineAPI.dto.CreatePaymentRequest
import com.university.MarathonOnlineAPI.dto.PaymentDTO
import com.university.MarathonOnlineAPI.exception.PaymentException
import com.university.MarathonOnlineAPI.service.PaymentService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payment")
class PaymentController(private val paymentService: PaymentService) {

    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    @PostMapping
    fun addPayment(@RequestBody @Valid newPayment: CreatePaymentRequest): ResponseEntity<Any> {
        return try {
            val addedPayment = paymentService.addPayment(newPayment)
            ResponseEntity(addedPayment, HttpStatus.OK)
        } catch (e: PaymentException) {
            logger.error("Error adding payment: ${e.message}")
            ResponseEntity("Payment error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun deletePayment(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            paymentService.deletePaymentById(id)
            logger.info("Payment with ID $id deleted successfully")
            ResponseEntity.ok("Payment with ID $id deleted successfully")
        } catch (e: PaymentException) {
            logger.error("Failed to delete payment with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to delete payment with ID $id: ${e.message}")
        } catch (e: Exception) {
            logger.error("Failed to delete payment with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete payment with ID $id: ${e.message}")
        }
    }

    @PutMapping
    fun updatePayment(@RequestBody @Valid paymentDTO: PaymentDTO): ResponseEntity<PaymentDTO> {
        return try {
            val updatedPayment = paymentService.updatePayment(paymentDTO)
            ResponseEntity(updatedPayment, HttpStatus.OK)
        } catch (e: PaymentException) {
            logger.error("Payment exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw PaymentException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating payment: ${e.message}")
            throw PaymentException("Error updating payment: ${e.message}")
        }
    }

    @GetMapping
    fun getPayments(): ResponseEntity<List<PaymentDTO>> {
        return try {
            val payments = paymentService.getPayments()
            ResponseEntity(payments, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getPayments: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    fun getPaymentById(@PathVariable id: Long): ResponseEntity<PaymentDTO> {
        return try {
            val foundPayment = paymentService.getById(id)
            ResponseEntity.ok(foundPayment)
        } catch (e: PaymentException) {
            logger.error("Error getting payment by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting payment by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
