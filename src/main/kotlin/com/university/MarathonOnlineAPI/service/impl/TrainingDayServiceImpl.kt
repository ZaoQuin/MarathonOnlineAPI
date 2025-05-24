package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.dto.TrainingDayDTO
import com.university.MarathonOnlineAPI.entity.Record
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.TrainingPlanException
import com.university.MarathonOnlineAPI.mapper.RecordMapper
import com.university.MarathonOnlineAPI.mapper.TrainingDayMapper
import com.university.MarathonOnlineAPI.repos.TrainingDayRepository
import com.university.MarathonOnlineAPI.repos.TrainingPlanRepository
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.TrainingDayService
import com.university.MarathonOnlineAPI.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TrainingDayServiceImpl(
    private val trainingDayRepository: TrainingDayRepository,
    private val trainingPlanRepository: TrainingPlanRepository,
    private val trainingDayMapper: TrainingDayMapper,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val recordMapper: RecordMapper,
) : TrainingDayService {

    private val logger = LoggerFactory.getLogger(TrainingDayServiceImpl::class.java)

    override fun getCurrentTrainingDayByJwt(jwt: String): TrainingDayDTO {
        val userDTO =
            tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")
        val now = LocalDateTime.now()
        val activePlan = trainingPlanRepository.findTopByUserIdAndStatusOrderByStartDateDesc(userDTO.id!!)
            ?: throw RuntimeException("No active training plan found for the user")
        val currentTrainingDay = activePlan.trainingDays.firstOrNull { day ->
            val dayDate = day.dateTime
            dayDate != null && dayDate.toLocalDate() == now.toLocalDate()
        } ?: throw RuntimeException("No training day found for today")

        // 3. Map entity về DTO trả về (giả sử bạn đã có mapper)
        return trainingDayMapper.toDto(currentTrainingDay)
    }

    override fun saveRecordIntoTrainingDay(recordDTO: RecordDTO, jwt: String): TrainingDayDTO {
        return try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")
            val now = LocalDateTime.now()
            val activePlan = trainingPlanRepository
                .findTopByUserIdAndStatusOrderByStartDateDesc(userDTO.id!!)
                ?: throw RuntimeException("No active training plan found for the user")

            val currentTrainingDay = activePlan.trainingDays.firstOrNull { day ->
                val dayDate = day.dateTime
                dayDate != null && dayDate.toLocalDate() == now.toLocalDate()
            } ?: throw RuntimeException("No training day found for today")

            val record = recordMapper.toEntity(recordDTO)

            if (currentTrainingDay.records.isNullOrEmpty()) {
                currentTrainingDay.records = mutableListOf(record)
            } else {
                val minutesDifference = getMinutesDifferenceFromLatestRecord(currentTrainingDay.records, record)
                val restMinute = currentTrainingDay.session?.type?.maxRestMinutes
                    ?: throw TrainingPlanException("Không xác định được loại bài tập")

                if (minutesDifference != null && minutesDifference <= restMinute) {
                    val records = currentTrainingDay.records?.toMutableList() ?: mutableListOf()
                    records.add(record)
                    currentTrainingDay.records = records
                } else {
                    throw TrainingPlanException("Không thêm record vì thời gian nghỉ đã vượt quá $restMinute phút, vui lòng luyện tập lại")
                }
            }

// ✅ currentTrainingDay được quản lý bởi JPA nên không gây lỗi
            val saved = trainingDayRepository.save(currentTrainingDay)
            return trainingDayMapper.toDto(saved)
        } catch (e: Exception) {
            throw Exception("Lỗi khi lưu record vào TrainingDay: ${e.message}")
        }
    }

    override fun resetTrainingDay(jwt: String): TrainingDayDTO {
        val userDTO =
            tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")
        val now = LocalDateTime.now()
        val activePlan = trainingPlanRepository
            .findTopByUserIdAndStatusOrderByStartDateDesc(userDTO.id!!)
            ?: throw RuntimeException("No active training plan found for the user")

        val currentTrainingDay = activePlan.trainingDays.firstOrNull { day ->
            val dayDate = day.dateTime
            dayDate != null && dayDate.toLocalDate() == now.toLocalDate()
        } ?: throw RuntimeException("No training day found for today")
        currentTrainingDay.records = mutableListOf()
        return trainingDayMapper.toDto(trainingDayRepository.save(currentTrainingDay))
    }

    fun getMinutesDifferenceFromLatestRecord(
        existingRecords: List<Record>?,
        newRecord: Record
    ): Long? {
        if (existingRecords.isNullOrEmpty() || newRecord.timestamp == null) return null

        val latestRecord = existingRecords.maxByOrNull { it.timestamp ?: LocalDateTime.MIN }

        return latestRecord?.timestamp?.let { latestTimestamp ->
            java.time.Duration.between(latestTimestamp, newRecord.timestamp).toMinutes()
        }
    }
}