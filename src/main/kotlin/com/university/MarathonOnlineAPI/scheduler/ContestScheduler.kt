package com.university.MarathonOnlineAPI.scheduler

import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.repos.ContestRepository
import com.university.MarathonOnlineAPI.repos.RegistrationRepository
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ContestScheduler(
    private val contestRepository: ContestRepository,
    private val registrationRepository: RegistrationRepository
) {
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    fun updateContestStatus() {
        val now = LocalDateTime.now()
        val contestsToUpdate = contestRepository.findAllByEndDateBeforeAndStatus(now, EContestStatus.ACTIVE)

        contestsToUpdate.forEach { contest ->
            println("Updating contest: ${contest.id}, current status: ${contest.status}")
            contest.status = EContestStatus.FINISHED
        }

        contestRepository.saveAll(contestsToUpdate)
        println("Updated status for ${contestsToUpdate.size} contests at $now")
    }
}