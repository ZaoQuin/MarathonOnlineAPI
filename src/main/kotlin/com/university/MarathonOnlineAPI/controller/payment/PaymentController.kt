package com.university.MarathonOnlineAPI.controller.payment

import com.university.MarathonOnlineAPI.config.VnPayProperties
import com.university.MarathonOnlineAPI.controller.StringResponse
import com.university.MarathonOnlineAPI.dto.CreatePaymentRequest
import com.university.MarathonOnlineAPI.dto.PaymentDTO
import com.university.MarathonOnlineAPI.entity.EPaymentStatus
import com.university.MarathonOnlineAPI.exception.PaymentException
import com.university.MarathonOnlineAPI.service.PaymentService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@RestController
@RequestMapping("/api/v1/payment")
class PaymentController(private val paymentService: PaymentService,
                        private val vnPayProperties: VnPayProperties) {

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

    @GetMapping("/create-vnpay")
    fun createVnPayUrl(
        @RequestParam amount: Int,
        @RequestParam registrationId: Long,
        request: HttpServletRequest
    ): ResponseEntity<StringResponse> {
        val vnpVersion = "2.1.0"
        val vnpCommand = "pay"
        val vnpTmnCode = vnPayProperties.tmnCode
        val vnpCurrCode = "VND"
        val vnpLocale = "vn"

        val vnpTxnRef = System.currentTimeMillis().toString() + Random.nextInt(1000, 9999)
        val vnpOrderInfo = "Thanh toan dang ky giai chay #$registrationId"
        val vnpOrderType = "other"
        val vnpAmount = amount * 100 // Đơn vị là xu

        val vnpReturnUrl = vnPayProperties.returnUrl
        val vnpIpAddr = request.remoteAddr
        val vnpCreateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

        // Bước 1: Tạo param
        val params = sortedMapOf(
            "vnp_Version" to vnpVersion,
            "vnp_Command" to vnpCommand,
            "vnp_TmnCode" to vnpTmnCode,
            "vnp_Amount" to vnpAmount.toString(),
            "vnp_CurrCode" to vnpCurrCode,
            "vnp_TxnRef" to vnpTxnRef,
            "vnp_OrderInfo" to vnpOrderInfo,
            "vnp_OrderType" to vnpOrderType,
            "vnp_Locale" to vnpLocale,
            "vnp_ReturnUrl" to vnpReturnUrl,
            "vnp_IpAddr" to vnpIpAddr,
            "vnp_CreateDate" to vnpCreateDate
        )

        // Bước 2: Tạo chuỗi dữ liệu để hash
        val hashData = params.map { "${it.key}=${it.value}" }.joinToString("&")
        val secureHash = hmacSHA512(vnPayProperties.hashSecret, hashData)

        // Bước 3: Tạo URL thanh toán
        val queryUrl = params.map { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }.joinToString("&")
        val paymentUrl = "${vnPayProperties.payUrl}?$queryUrl&vnp_SecureHash=$secureHash"

        return ResponseEntity.ok(StringResponse(paymentUrl))
    }

    @GetMapping("/vnpay-return")
    fun vnPayReturn(@RequestParam allParams: Map<String, String>): ResponseEntity<CreatePaymentRequest> {
        val responseCode = allParams["vnp_ResponseCode"]
        val amountStr = allParams["vnp_Amount"]
        val txnRef = allParams["vnp_TxnRef"]
        val bankCode = allParams["vnp_BankCode"]

        val payment = CreatePaymentRequest(
            amount = amountStr?.toBigDecimal()?.divide(BigDecimal(100)),
            paymentDate = LocalDateTime.now(),
            transactionRef = txnRef,
            responseCode = responseCode,
            bankCode = bankCode,
            status = if (responseCode == "00") EPaymentStatus.SUCCESS else EPaymentStatus.FAILED,
            registrationId = allParams["vnp_OrderInfo"]?.toLongOrNull()
        )

        return ResponseEntity.ok(payment)
    }

    private fun hmacSHA512(secretKey: String, data: String): String {
        val hmac = MessageDigest.getInstance("SHA-512")
        hmac.update(secretKey.toByteArray(Charsets.UTF_8))
        return hmac.digest(data.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
