package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long>
