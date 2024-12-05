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
            awardPrizes(contest)
        }

        println("Updated status for ${contestsToUpdate.size} contests at $now")
    }

    private fun awardPrizes(contest: Contest) {
        val sortedRegistrations = contest.registrations
            ?.filter { it.status == ERegistrationStatus.COMPLETED }
            ?.sortedWith(
                compareByDescending<Registration> { reg ->
                    reg.races?.sumOf { it.distance?.toDouble() ?: 0.0 } ?: 0.0
                }.thenBy { reg ->
                    reg.races?.sumOf { it.timeTaken?.toLong() ?: 0L } ?: 0L
                }.thenBy { reg ->
                    reg.races?.map { it.avgSpeed?.toDouble() ?: 0.0 }?.average() ?: 0.0
                }.thenBy { reg ->
                    reg.registrationDate
                }
            ) ?: emptyList()

        val rewardsByRank = contest.rewards?.groupBy { it.rewardRank } ?: emptyMap()

        rewardsByRank.filterKeys { it != 0 && it != null }.forEach { (rank, rewards) ->
            sortedRegistrations.getOrNull(rank!! - 1)?.let { registration ->
                assignRewardsToRegistration(registration, rewards)
            }
        }

        val defaultRewards = rewardsByRank[0] ?: emptyList()
        sortedRegistrations.forEach { registration ->
            assignRewardsToRegistration(registration, defaultRewards)
        }

        registrationRepository.saveAll(sortedRegistrations)
    }

    private fun assignRewardsToRegistration(registration: Registration, rewards: List<Reward>) {
        registration.rewards = registration.rewards?.toMutableList()?.apply {
            addAll(rewards)
        } ?: rewards.toMutableList()
    }

}