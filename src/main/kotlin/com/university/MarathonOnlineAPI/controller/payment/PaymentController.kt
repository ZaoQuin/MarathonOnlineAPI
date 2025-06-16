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
        val vnp_Version = "2.1.0"
        val vnp_Command = "pay"
        val vnp_TxnRef = UUID.randomUUID().toString().substring(0, 8)
        val vnp_OrderInfo = registrationId.toString()
        val vnp_Amount = (amount * 100).toString()

        val vnp_IpAddr = getClientIpAddress(request)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"))
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        val vnp_CreateDate = formatter.format(calendar.time)
        calendar.add(Calendar.MINUTE, 15)
        val vnp_ExpireDate = formatter.format(calendar.time)

        val vnp_Params = TreeMap<String, String>()
        vnp_Params["vnp_Version"] = vnp_Version
        vnp_Params["vnp_Command"] = vnp_Command
        vnp_Params["vnp_TmnCode"] = vnPayProperties.tmnCode
        vnp_Params["vnp_Amount"] = vnp_Amount
        vnp_Params["vnp_CurrCode"] = "VND"
        vnp_Params["vnp_TxnRef"] = vnp_TxnRef
        vnp_Params["vnp_OrderInfo"] = vnp_OrderInfo
        vnp_Params["vnp_OrderType"] = "other"
        vnp_Params["vnp_Locale"] = "vn"
        vnp_Params["vnp_ReturnUrl"] = vnPayProperties.returnUrl
        vnp_Params["vnp_CreateDate"] = vnp_CreateDate
        vnp_Params["vnp_ExpireDate"] = vnp_ExpireDate
        vnp_Params["vnp_IpAddr"] = vnp_IpAddr

        val hashData = vnp_Params.entries.joinToString("&") { (key, value) ->
            if (value.isNotEmpty()) {
                "$key=${URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()).replace("+", "%20")}"
            } else {
                ""
            }
        }.trim('&')

        logger.info("HashData for createVnPayUrl: $hashData")
        val secureHash = hmacSHA512(vnPayProperties.hashSecret, hashData)
        logger.info("Generated SecureHash: $secureHash")

        val paymentUrl = "${vnPayProperties.payUrl}?$hashData&vnp_SecureHash=$secureHash"
        logger.info("Payment URL: $paymentUrl")

        return ResponseEntity.ok(StringResponse(paymentUrl))
    }

    @GetMapping("/vnpay-return")
    fun vnPayReturn(@RequestParam allParams: Map<String, String>): ResponseEntity<CreatePaymentRequest> {
        logger.info("VNPay Return Params: $allParams")
        val secureHash = allParams["vnp_SecureHash"] ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()

        val filteredParams = allParams.filterKeys {
            it != "vnp_SecureHash" && it != "vnp_SecureHashType"
        }.toSortedMap()

        val hashData = filteredParams.entries.joinToString("&") { (key, value) ->
            if (value.isNotEmpty()) {
                "$key=${URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()).replace("+", "%20")}"
            } else {
                ""
            }
        }.trim('&')

        logger.info("HashData for vnPayReturn: $hashData")
        val generatedHash = hmacSHA512(vnPayProperties.hashSecret, hashData)
        logger.info("Generated SecureHash: $generatedHash")
        logger.info("Received SecureHash: $secureHash")

        if (secureHash != generatedHash) {
            logger.error("Invalid signature: expected $secureHash, got $generatedHash")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }

        val orderInfo = allParams["vnp_OrderInfo"] ?: "unknown"
        val responseCode = allParams["vnp_ResponseCode"] ?: "99"

        val dto = CreatePaymentRequest(
            amount = (allParams["vnp_Amount"]?.toInt()?.div(100) ?: 0).toBigDecimal(),
            transactionRef = allParams["vnp_TxnRef"] ?: "unknown",
            responseCode = responseCode,
            bankCode = allParams["vnp_BankCode"],
            paymentDate = try {
                val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                val payDateStr = allParams["vnp_PayDate"]
                if (!payDateStr.isNullOrBlank()) {
                    LocalDateTime.parse(payDateStr, formatter)
                } else {
                    LocalDateTime.now()
                }
            } catch (e: Exception) {
                logger.error("Error parsing payment date: ${e.message}")
                LocalDateTime.now()
            },
            status = if (responseCode == "00") EPaymentStatus.SUCCESS else EPaymentStatus.FAILED,
            registrationId = orderInfo.toLongOrNull() ?: -1L
        )

        return ResponseEntity.ok(dto)
    }

    // Hàm lấy IP client thực tế
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrEmpty()) {
            return xForwardedFor.split(",")[0].trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrEmpty()) {
            return xRealIp
        }

        return request.remoteAddr ?: "127.0.0.1"
    }

    fun hmacSHA512(key: String, data: String): String {
        val hmacSHA512 = Mac.getInstance("HmacSHA512")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "HmacSHA512")
        hmacSHA512.init(secretKeySpec)
        val hashBytes = hmacSHA512.doFinal(data.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
