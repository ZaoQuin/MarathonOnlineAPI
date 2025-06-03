package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.dto.TrainingDayDTO
import com.university.MarathonOnlineAPI.entity.ETrainingDayStatus
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
            val userDTO = tokenService.extractEmail(jwt)?.let { email ->
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

            if (currentTrainingDay.status != ETrainingDayStatus.ACTIVE) {
                throw RuntimeException("Training day is not active")
            }

            val records = currentTrainingDay.records?.toMutableList() ?: mutableListOf()
            records.add(record)
            currentTrainingDay.records = records

            val session = currentTrainingDay.session ?: throw RuntimeException("No session associated with training day")
            val targetDistance = session.distance ?: throw RuntimeException("Session distance not specified")
            val actualDistance = record.distance ?: throw RuntimeException("Record distance not specified")

            val completionPercentage = (actualDistance / targetDistance * 100).coerceIn(0.0, 100.0)
            currentTrainingDay.completionPercentage = completionPercentage

            currentTrainingDay.status = when {
                completionPercentage >= 100 -> ETrainingDayStatus.COMPLETED
                completionPercentage > 0 -> ETrainingDayStatus.PARTIALLY_COMPLETED
                else -> ETrainingDayStatus.ACTIVE
            }

            val saved = trainingDayRepository.save(currentTrainingDay)
            trainingDayMapper.toDto(saved)
        } catch (e: Exception) {
            throw Exception("Error saving record into TrainingDay: ${e.message}")
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
}