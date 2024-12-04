package com.university.MarathonOnlineAPI.scheduler

import com.university.MarathonOnlineAPI.entity.EContestStatus
import com.university.MarathonOnlineAPI.repos.ContestRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ContestScheduler(
    private val repository: ContestRepository
) {

    @Scheduled(cron = "0 0 0 * * ?")
    fun updateContestStatus() {
        val now = LocalDateTime.now()
        val contestsToUpdate = repository.findAllByEndDateBeforeAndStatus(now, EContestStatus.ACTIVE)

        contestsToUpdate.forEach { contest ->
            contest.status = EContestStatus.FINISHED
        }

        repository.saveAll(contestsToUpdate)
        println("Updated status for ${contestsToUpdate.size} contests at $now")
    }
}