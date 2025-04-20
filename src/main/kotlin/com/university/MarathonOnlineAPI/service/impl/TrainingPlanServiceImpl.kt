package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.TrainingPlanDTO
import com.university.MarathonOnlineAPI.dto.TrainingPlanInputDTO
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.mapper.TrainingPlanMapper
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
    private val aiTrainingPlanService: AITrainingPlanService
): TrainingPlanService {

    override fun createTrainingPlan(inputDTO: TrainingPlanInputDTO, userId: Long): TrainingPlanDTO {
        val currentUser = userRepository.findById(userId)
            .orElseThrow();

        // Chuyển DTO sang entity
        val input = TrainingPlanInput().apply {
            level = inputDTO.level
            goal = inputDTO.goal
            maxDistance = inputDTO.maxDistance
            averagePace = inputDTO.averagePace
            weeks = inputDTO.weeks
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

        // Lưu plan
        val savedPlan = trainingPlanRepository.save(plan)

        // Tạo lịch trình với AI
        val trainingDays = aiTrainingPlanService.generateTrainingDays(savedInput, savedPlan)

        // Cập nhật ngày bắt đầu và kết thúc
        savedPlan.trainingDays = trainingDays
        savedPlan.startDate = LocalDateTime.now()
        savedPlan.endDate = savedPlan.startDate?.plusWeeks(inputDTO.weeks!!.toLong())

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
            ETrainingPlanInputGoal.FINISH -> "về đích"
            ETrainingPlanInputGoal.TIME -> "phá kỷ lục thời gian"
            ETrainingPlanInputGoal.NO_INJURY -> "không chấn thương"
            else -> "không rõ mục tiêu"
        }

        val weeks = input.weeks ?: 0
        val year = LocalDateTime.now().year

        return "$userName - $level, $weeks tuần, mục tiêu $goal ($year)"
    }

}
