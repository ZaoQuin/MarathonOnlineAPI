package com.university.MarathonOnlineAPI.squartz

import com.university.MarathonOnlineAPI.entity.ERegistrationStatus
import com.university.MarathonOnlineAPI.repos.RegistrationRepository
import com.university.MarathonOnlineAPI.service.RegistrationService
import jakarta.transaction.Transactional
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class DeleteUnpaidRegistrationJob(
    private val registrationService: RegistrationService
) : Job {

    @Transactional
    override fun execute(context: JobExecutionContext) {
        val registrationId = context.jobDetail.jobDataMap["registrationId"] as Long
        try {
            val registration = registrationService.getById(registrationId)
            if (registration.status == ERegistrationStatus.PENDING) {
                println("🔴 Xoá đăng ký chưa thanh toán ID: $registrationId")
                registrationService.deleteRegistrationById(registrationId)
            } else {
                println("⚠️ Registration ID $registrationId không ở trạng thái PENDING, không xoá.")
            }
        } catch (ex: Exception) {
            println("❌ Lỗi khi xoá registration ID $registrationId: ${ex.message}")
            ex.printStackTrace()
        }
    }
}
