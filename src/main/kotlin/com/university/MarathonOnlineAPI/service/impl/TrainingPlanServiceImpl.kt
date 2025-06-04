package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.TrainingPlanDTO
import com.university.MarathonOnlineAPI.dto.TrainingPlanInputDTO
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.mapper.TrainingPlanMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.*
import com.university.MarathonOnlineAPI.view.SingleTrainingPlanView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import jakarta.transaction.Transactional

@Service
class TrainingPlanServiceImpl(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val trainingPlanMapper: TrainingPlanMapper,
    private val trainingPlanInputRepository: TrainingPlanInputRepository,
    private val trainingDayRepository: TrainingDayRepository,
    private val userRepository: UserRepository,
    private val aiTrainingPlanService: AITrainingPlanService,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val userMapper: UserMapper,
    private val recordService: RecordService
): TrainingPlanService {
    override fun createTrainingPlan(inputDTO: TrainingPlanInputDTO, jwt: String): TrainingPlanDTO {
        val userDTO = tokenService.extractEmail(jwt)?.let { email ->
            userService.findByEmail(email)
        } ?: throw AuthenticationException("Email not found in the token")

        val currentUser = userMapper.toEntity(userDTO)

        val runningStat = recordService.getRunningStatsByUser(currentUser.id!!)

        val input = TrainingPlanInput().apply {
            level = inputDTO.level
            goal = inputDTO.goal
            maxDistance = runningStat?.maxDistance
            averagePace = runningStat?.averagePace
            trainingWeeks = inputDTO.trainingWeeks
            user = currentUser
        }

        val savedInput = trainingPlanInputRepository.save(input)

        val plan = TrainingPlan(
            name = generatePlanName(input, currentUser.fullName ?: "Runner"),
            user = currentUser,
            input = savedInput,
            createdAt = LocalDateTime.now(),
            status = ETrainingPlanStatus.ACTIVE,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusWeeks(input.trainingWeeks?.toLong() ?: 4)
        )

        val activePlans = trainingPlanRepository.findByUserIdAndStatus(currentUser.id!!, ETrainingPlanStatus.ACTIVE)
        activePlans.forEach { it.status = ETrainingPlanStatus.ARCHIVED }
        trainingPlanRepository.saveAll(activePlans)

        val savedPlan = trainingPlanRepository.save(plan)

        // Tạo TrainingDay cho ngày đầu tiên
        val firstDay = aiTrainingPlanService.generateTrainingDayForDate(savedInput, savedPlan, savedPlan.startDate!!)
        savedPlan.trainingDays = mutableListOf(firstDay)

        val final = trainingPlanRepository.findById(savedPlan.id!!)
        return trainingPlanMapper.toDto(final.orElseThrow())
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

    @Transactional
    override fun getTrainingPlanByJwt(jwt: String): TrainingPlanDTO {
        return try {
            val email = tokenService.extractEmail(jwt)
                ?: throw AuthenticationException("Email not found in the token")

            val userDTO = userService.findByEmail(email)
            val now = LocalDateTime.now()

            val plan = trainingPlanRepository.findByUserIdAndStatusAndStartDateBeforeAndEndDateAfter(
                userDTO.id!!,
                ETrainingPlanStatus.ACTIVE,
                now,
                now
            ) ?: throw IllegalStateException("No matching training plan found.")

            trainingPlanMapper.toDto(plan)
        } catch (e: Exception) {
            println("❌ Error getTrainingPlanByJwt: ${e.message}")
            throw e
        }
    }

    override fun getPlansByStatus(
        pageable: Pageable,
        status: ETrainingPlanStatus,
        jwt: String,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): Page<SingleTrainingPlanView> {
        val email = tokenService.extractEmail(jwt)
            ?: throw AuthenticationException("Email not found in token")

        val user = userService.findByEmail(email)

        return trainingPlanRepository.findProjectedByFilters(user.id!!, status, startDate, endDate, pageable)
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
            ETrainingPlanInputGoal.MARATHON_FINISH -> "về đích marathon"
            ETrainingPlanInputGoal.MARATHON_TIME -> "phá kỷ lục thời gian marathon"
            ETrainingPlanInputGoal.HALF_MARATHON_FINISH -> "về đích half marathon"
            ETrainingPlanInputGoal.HALF_MARATHON_TIME -> "phá kỷ lục thời gian half marathon"
            ETrainingPlanInputGoal.TEN_KM_FINISH -> "hoàn thành 10 km"
            ETrainingPlanInputGoal.TEN_KM_TIME -> "phá kỷ lục thời gian 10 km"
            ETrainingPlanInputGoal.FIVE_KM_FINISH -> "hoàn thành 5 km"
            ETrainingPlanInputGoal.FIVE_KM_TIME -> "phá kỷ lục thời gian 5 km"
            else -> "mục tiêu khác"
        }

        val weeks = input.trainingWeeks
        val year = LocalDateTime.now().year

        return "$userName - $level, $weeks tuần, mục tiêu $goal ($year)"
    }

}
