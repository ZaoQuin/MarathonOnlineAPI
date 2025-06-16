package com.university.MarathonOnlineAPI.squartz

import com.university.MarathonOnlineAPI.entity.ERegistrationStatus
import com.university.MarathonOnlineAPI.repos.RegistrationRepository
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class DeleteUnpaidRegistrationJob(
    private val  registrationRepository: RegistrationRepository
) : Job {
    override fun execute(context: JobExecutionContext) {
        val registrationId = context.jobDetail.jobDataMap["registrationId"] as Long

        val registrationOpt = registrationRepository.findById(registrationId)
        if (registrationOpt.isPresent) {
            val registration = registrationOpt.get()
            if (registration.status == ERegistrationStatus.PENDING) {
                println("🔴 Xoá đăng ký chưa thanh toán ID: $registrationId")

                registration.payment = null
                registrationRepository.delete(registration)
            }
        }
    }
}
