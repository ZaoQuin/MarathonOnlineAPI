package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.PaymentDTO
import com.university.MarathonOnlineAPI.entity.Payment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.modelmapper.ModelMapper
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentMapperTest {

    @Mock
    private lateinit var modelMapper: ModelMapper

    @InjectMocks
    private lateinit var paymentMapper: PaymentMapper

    private lateinit var payment: Payment
    private lateinit var paymentDTO: PaymentDTO

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        payment = Payment(
            id = 1L,
            amount = BigDecimal(100.50),
            paymentDate = LocalDateTime.now(),
//            status = EPaymentStatus.COMPLETED
        )

        paymentDTO = PaymentDTO(
            id = 1L,
            amount = BigDecimal(100.50),
            paymentDate = LocalDateTime.now(),
//            status = EPaymentStatus.COMPLETED
        )
    }

    @Test
    fun `should map Payment to PaymentDTO`() {
        Mockito.`when`(modelMapper.map(payment, PaymentDTO::class.java)).thenReturn(paymentDTO)

        val result = paymentMapper.toDto(payment)

        assertEquals(payment.id, result.id)
        assertEquals(payment.amount, result.amount)
        assertEquals(payment.paymentDate, result.paymentDate)
//        assertEquals(payment.status, result.status)
    }

    @Test
    fun `should map PaymentDTO to Payment`() {
        Mockito.`when`(modelMapper.map(paymentDTO, Payment::class.java)).thenReturn(payment)

        val result = paymentMapper.toEntity(paymentDTO)

        assertEquals(paymentDTO.id, result.id)
        assertEquals(paymentDTO.amount, result.amount)
        assertEquals(paymentDTO.paymentDate?.second, result.paymentDate?.second)
//        assertEquals(paymentDTO.status, result.status)
    }
}
