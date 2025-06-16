package com.university.MarathonOnlineAPI.squartz

import com.university.MarathonOnlineAPI.entity.Registration
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class DeleteUnpaidRegistrationService(
    private val scheduler: Scheduler
) {
    fun scheduleDeleteIfUnpaid(registration: Registration) {
        val registrationId = registration.id ?: return
        val registrationTime = registration.registrationDate ?: return
        val contestStartTime = registration.contest?.registrationDeadline ?: return

        val now = LocalDateTime.now()
        val deleteAt = if (registrationTime.plusHours(24).isBefore(contestStartTime)) {
            registrationTime.plusHours(24)
        } else {
            contestStartTime.minusMinutes(1)
        }

        val deleteAtDate = Date.from(deleteAt.atZone(ZoneId.systemDefault()).toInstant())

        println("Thời gian xóa: $deleteAtDate")

        if (deleteAtDate.before(Date())) {
            println("❌ Không schedule job vì thời điểm xoá đã qua. ID: $registrationId")
            return
        }


        val jobDetail = JobBuilder.newJob(DeleteUnpaidRegistrationJob::class.java)
            .withIdentity("RegistrationJob_${registrationId}")
            .usingJobData("registrationId", registrationId)
            .storeDurably()
            .build()

        val trigger = TriggerBuilder.newTrigger()
            .withIdentity("RegistrationTrigger_$registrationId")
            .startAt(deleteAtDate)
            .build()

        scheduler.scheduleJob(jobDetail, trigger)
    }

    fun cancelScheduledOrderUpdate(registrationId: Long): Boolean {
        val jobKey = JobKey.jobKey("RegistrationJob_$registrationId")
        return if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey)
            true
        } else {
            false
        }
    }
}