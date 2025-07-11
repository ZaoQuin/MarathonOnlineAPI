package com.university.MarathonOnlineAPI.handler

import com.university.MarathonOnlineAPI.dto.NotificationDTO
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.entity.ENotificationType
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class NotificationHandler {
    fun notifyRejectedRecord(record: RecordDTO): NotificationDTO {
        val title = "Bản ghi không hợp lệ."
        val content = """
            Bản ghi từ ${record.startTime} đến ${record.endTime} không được chấp nhận do chưa đáp ứng các tiêu chí của hệ thống.
            Nếu bạn cho rằng có nhầm lẫn, hãy phản hồi để được hỗ trợ thêm.
        """.trimIndent()


        return NotificationDTO(
            receiver = record.user,
            title = title,
            objectId = record.id,
            content = content,
            createAt = LocalDateTime.now(),
            isRead = false,
            type = ENotificationType.REJECTED_RECORD
        )
    }
}