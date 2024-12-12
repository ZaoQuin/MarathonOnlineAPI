package com.university.MarathonOnlineAPI.scheduler

import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.repos.ContestRepository
import com.university.MarathonOnlineAPI.repos.RegistrationRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ContestScheduler(
    private val contestRepository: ContestRepository,
    private val registrationRepository: RegistrationRepository
) {
    @Scheduled(cron = "0 0 0 * * ?")
    fun updateContestStatus() {
        val now = LocalDateTime.now()
        val contestsToUpdate = contestRepository.findAllByEndDateBeforeAndStatus(now, EContestStatus.ACTIVE)

        contestsToUpdate.forEach { contest ->
            contest.status = EContestStatus.FINISHED
            contestRepository.save(contest)
        }

        println("Updated status for ${contestsToUpdate.size} contests at $now")
    }
}