package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.TrainingPlanDTO
import com.university.MarathonOnlineAPI.dto.TrainingPlanInputDTO
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.mapper.TrainingPlanMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class TrainingPlanServiceImpl(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val trainingPlanMapper: TrainingPlanMapper,
    private val trainingPlanInputRepository: TrainingPlanInputRepository,
    private val trainingSessionRepository: TrainingSessionRepository,
    private val trainingDayRepository: TrainingDayRepository,
    private val userRepository: UserRepository,
    private val aiTrainingPlanService: AITrainingPlanService,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val recordService: RecordService
): TrainingPlanService {
    override fun createTrainingPlan(inputDTO: TrainingPlanInputDTO, userId: Long): TrainingPlanDTO {
        val currentUser = userRepository.findById(userId)
            .orElseThrow();

        val runningStat = recordService.getRunningStatsByUser(userId)

        // Chuyển DTO sang entity
        val input = TrainingPlanInput().apply {
            level = inputDTO.level
            goal = inputDTO.goal
            maxDistance = runningStat?.maxDistance
            averagePace = runningStat?.averagePace
            daysPerWeek = inputDTO.daysPerWeek
            user = currentUser
        }

        // Lưu input trước
        val savedInput = trainingPlanInputRepository.save(input)

        // Tạo plan
        val plan = TrainingPlan(
            name = generatePlanName(input, currentUser.fullName ?: "Runner"),
            user = currentUser,
            input = savedInput,
            createdAt = LocalDateTime.now(),
            status = ETrainingPlanStatus.ACTIVE,
        )


        val activePlans = trainingPlanRepository.findByUserIdAndStatus(userId, ETrainingPlanStatus.ACTIVE)
        activePlans.forEach {
            it.status = ETrainingPlanStatus.ARCHIVED
        }
        trainingPlanRepository.saveAll(activePlans)

        // Lưu plan
        val savedPlan = trainingPlanRepository.save(plan)

        // Tạo lịch trình với AI
        val trainingDays = aiTrainingPlanService.generateTrainingDays(savedInput, savedPlan)

        // Cập nhật ngày bắt đầu và kết thúc
        savedPlan.trainingDays = trainingDays
        savedPlan.startDate = LocalDateTime.now()
        savedPlan.endDate = savedPlan.startDate?.plusWeeks(4)

        return trainingPlanMapper.toDto(trainingPlanRepository.save(savedPlan))
    }

    override fun getUserTrainingPlans(userId: Long): List<TrainingPlanDTO> {
        val plans = trainingPlanRepository.findByUserId(userId)
        return plans.map { trainingPlanMapper.toDto(it) }
    }

    override fun getTrainingPlanById(planId: Long): TrainingPlanDTO {
        val plan = trainingPlanRepository.findById(planId).orElseThrow {
            NoSuchElementException("Training plan not found with ID: $planId")
        }
        return trainingPlanMapper.toDto(plan)
    }

    override fun getTrainingPlanByJwt(jwt: String): TrainingPlanDTO {
        val userDTO =
            tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")

        val now = LocalDateTime.now()
        val plan = trainingPlanRepository.findByUserIdAndStatusAndStartDateBeforeAndEndDateAfter(
            userDTO.id!!,
            ETrainingPlanStatus.ACTIVE,
            now,
            now
        )
        return plan?.let { trainingPlanMapper.toDto(it) }!!
    }

    fun getUserPlans(userId: Long): List<TrainingPlan> {
        val currentUser = userRepository.findById(userId).orElseThrow()
        return trainingPlanRepository.findByUserIdOrderByCreatedAtDesc(currentUser.id!!)
    }

    fun getPlanById(planId: Long): TrainingPlan {
        return trainingPlanRepository.findById(planId)
            .orElseThrow { Exception("Training plan not found") }
    }

    fun getPlanDays(planId: Long): List<TrainingDay> {
        return trainingDayRepository.findByPlanIdOrderByWeekAscDayOfWeekAsc(planId)
    }

    fun updatePlanStatus(planId: Long, status: String): TrainingPlan {
        val plan = trainingPlanRepository.findById(planId)
            .orElseThrow { Exception("Training plan not found") }

        plan.status = ETrainingPlanStatus.valueOf(status)
        return trainingPlanRepository.save(plan)
    }

    private fun generatePlanName(input: TrainingPlanInput, userName: String): String {
        val level = when (input.level) {
            ETrainingPlanInputLevel.BEGINNER -> "Người mới"
            ETrainingPlanInputLevel.INTERMEDIATE -> "Trung cấp"
            ETrainingPlanInputLevel.ADVANCED -> "Nâng cao"
            else -> "Không rõ"
        }

        val goal = when (input.goal) {
            ETrainingPlanInputGoal.MARATHON_FINISH -> "về đích"
            ETrainingPlanInputGoal.MARATHON_TIME -> "phá kỷ lục thời gian"
            ETrainingPlanInputGoal.NO_INJURY -> "không chấn thương"
            ETrainingPlanInputGoal.OTHER -> "mục tiêu khác"
            else -> "không rõ mục tiêu"
        }

        val weeks = 4
        val year = LocalDateTime.now().year

        return "$userName - $level, $weeks tuần, mục tiêu $goal ($year)"
    }

}
