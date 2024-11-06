package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long>
