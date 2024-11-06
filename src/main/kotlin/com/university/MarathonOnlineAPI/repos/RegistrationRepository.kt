package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Registration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RegistrationRepository : JpaRepository<Registration, Long>
