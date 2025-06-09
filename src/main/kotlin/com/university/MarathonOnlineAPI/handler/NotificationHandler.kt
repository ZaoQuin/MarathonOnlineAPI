package com.university.MarathonOnlineAPI.handler

import com.university.MarathonOnlineAPI.dto.FeedbackDTO
import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.ENotificationType
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class NotificationHandler {
    fun notifyRejectedRecord(record: RecordDTO): NotificationDTO {
        val title = "Phát hiện gian lận."
        val content = "Record \"${record.startTime}\" của bạn đã bị từ chối. Vui lòng kiểm tra lại."

        val notification = NotificationDTO(
            receiver = record.user,
            title = title,
            content = content,
            createAt = LocalDateTime.now(),
            isRead = false,
            type = ENotificationType.REJECTED_RECORD,
            objectId = record.id
        )

        return notification
    }
}