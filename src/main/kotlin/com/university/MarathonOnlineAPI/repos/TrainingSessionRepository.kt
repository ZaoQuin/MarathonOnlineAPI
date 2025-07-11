package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.TrainingSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrainingSessionRepository : JpaRepository<TrainingSession, Long> {

}
