package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.TrainingSessionDTO
import com.university.MarathonOnlineAPI.entity.TrainingSession
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.LocalTime

@Component
class TrainingSessionMapper(private val modelMapper: ModelMapper): Mapper<TrainingSessionDTO, TrainingSession> {

    override fun toDto(entity: TrainingSession): TrainingSessionDTO {
        val trainingSessionDTO = modelMapper.map(entity, TrainingSessionDTO::class.java)

        // Getting and logging trainingDays size
        println("TrainingDays count: ${entity.trainingDays.size}")

        // Process dateTime only if trainingDays exist
        if (entity.trainingDays.isNotEmpty()) {
            val trainingDay = entity.trainingDays.firstOrNull()
            trainingDay?.let { day ->
                day.plan?.startDate?.let { startDate ->
                    // Tính toán dateTime dựa vào startDate, week và dayOfWeek
                    val dateTime = calculateSessionDateTime(startDate, day.week!!, day.dayOfWeek!!, entity)
                    trainingSessionDTO.dateTime = dateTime
                }
            }
        }

        return trainingSessionDTO
    }

    override fun toEntity(dto: TrainingSessionDTO): TrainingSession {
        val trainingSession = modelMapper.map(dto, TrainingSession::class.java)
        return trainingSession
    }

    private fun calculateSessionDateTime(
        startDate: LocalDateTime,
        week: Int,
        dayOfWeek: Int,
        entity: TrainingSession
    ): LocalDateTime {
        // Ngày bắt đầu tuần 1 là chính là startDate.toLocalDate()
        val baseDate = startDate.toLocalDate()

        // Tính số ngày từ tuần và thứ: (week - 1) * 7 + (dayOfWeek - 1)
        // dayOfWeek: 1 = thứ 2, 7 = chủ nhật => (1 - 1) = 0 => đúng thứ đầu tuần
        val daysToAdd = ((week - 1) * 7L) + (dayOfWeek - 1)

        val sessionDate = baseDate.plusDays(daysToAdd)

        // Chọn giờ theo loại buổi tập
        val sessionTime = when (entity.type.name) {
            "LONG_RUN" -> LocalTime.of(7, 0)
            "SPEED_WORK" -> LocalTime.of(17, 0)
            "RECOVERY_RUN" -> LocalTime.of(6, 30)
            else -> LocalTime.of(8, 0)
        }

        return LocalDateTime.of(sessionDate, sessionTime)
    }
}