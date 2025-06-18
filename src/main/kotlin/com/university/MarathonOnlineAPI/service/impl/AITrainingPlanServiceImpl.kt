package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.config.AITrainingProperties
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.repos.*
import com.university.MarathonOnlineAPI.service.AITrainingPlanService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.io.IOException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern

@Service
class AITrainingPlanServiceImpl(
    private val trainingSessionRepository: TrainingSessionRepository,
    private val trainingDayRepository: TrainingDayRepository,
    private val aiTrainingProperties: AITrainingProperties
) : AITrainingPlanService {
    val apiKey = aiTrainingProperties.api
    private val GROQ_MODEL = "llama3-70b-8192"

    override fun generateTrainingDayForDate(input: TrainingPlanInput, plan: TrainingPlan, date: LocalDateTime): TrainingDay {
        val previousDays = trainingDayRepository.findByPlanIdAndDateTimeBefore(plan.id!!, date)
        val feedbackData = previousDays.mapNotNull { it.trainingFeedback }
        val recordData: List<Record> = previousDays.mapNotNull { it.record }

        val prompt = createPromptForDailyTraining(input, plan, date, feedbackData, recordData)
        val aiResponse = callAIApi(prompt)
        val trainingDay = parseDailyAIResponse(aiResponse, plan, date)

        return trainingDayRepository.save(trainingDay)
    }

    private fun createPromptForDailyTraining(
        input: TrainingPlanInput,
        plan: TrainingPlan,
        date: LocalDateTime,
        feedbackData: List<TrainingFeedback>,
        recordData: List<Record>
    ): String {
        val daysSinceStart = ChronoUnit.DAYS.between(plan.startDate, date)
        val week = (daysSinceStart / 7 + 1).toInt()
        val dayOfWeek = date.dayOfWeek.value

        val avgDifficulty = feedbackData.map { it.difficultyRating }.groupingBy { it }.eachCount()
        val avgFeeling = feedbackData.map { it.feelingRating }.groupingBy { it }.eachCount()
        val difficultySummary = avgDifficulty.entries.joinToString { "${it.key}: ${it.value}" }
        val feelingSummary = avgFeeling.entries.joinToString { "${it.key}: ${it.value}" }

        val avgDistance = recordData.mapNotNull { it.distance }.average().let { if (it.isNaN()) 0.0 else it }
        val avgPace = recordData.mapNotNull { it.avgSpeed }.average().let { if (it.isNaN()) 0.0 else it }

        // Lấy thông tin chuẩn từ Hal Higdon dựa trên mục tiêu và trình độ
        val trainingStandards = getHalHigdonStandards(input.goal!!, input.level!!)

        return """
        Tạo một ngày tập luyện marathon được cá nhân hóa cho ngày ${date.toLocalDate()} dựa trên phương pháp Hal Higdon và thông tin sau:
        
        THÔNG TIN RUNNER:
        • Trình độ: ${input.level}
        • Mục tiêu: ${input.goal}
        • Khoảng cách dài nhất từng chạy: ${input.maxDistance ?: 10.0} km
        • Tốc độ trung bình: ${input.averagePace ?: 6.0} phút/km
        • Số tuần luyện tập: ${input.trainingWeeks ?: trainingStandards.totalWeeks}
        • Tuần hiện tại: $week/${trainingStandards.totalWeeks}
        • Ngày trong tuần: $dayOfWeek (1=Thứ Hai, 7=Chủ Nhật)
        
        CHUẨN HAL HIGDON CHO ${input.goal} - ${input.level}:
        • Tổng thời gian: ${trainingStandards.totalWeeks} tuần
        • Tuần điển hình: ${trainingStandards.weeklyStructure}
        • Longest Workout: ${trainingStandards.longestWorkout}
        • Số ngày chạy/tuần: ${trainingStandards.runDaysPerWeek}
        
        DỮ LIỆU TỪ CÁC BUỔI TẬP TRƯỚC:
        • Độ khó trung bình: $difficultySummary
        • Cảm giác trung bình: $feelingSummary
        • Khoảng cách trung bình: ${avgDistance.round(2)} km
        • Tốc độ trung bình: ${avgPace.round(2)} phút/km
        
        NGUYÊN TẮC HUẤN LUYỆN HAL HIGDON:
        • Tuần điển hình: ${trainingStandards.weeklyStructure}
        • LONG_RUN: Chủ nhật, tăng dần theo chu kỳ 3 tuần tăng + 1 tuần giảm
        • SPEED_WORK: ${getSpeedWorkGuidance(input.level!!, input.goal!!)}
        • RECOVERY_RUN: Pace chậm hơn mục tiêu 60-90 giây/km
        • Nếu feedback báo TIRED/EXHAUSTED hoặc độ khó HARD/VERY_HARD, áp dụng nguyên tắc deload
        
        PHÂN BỐ TUẦN THEO HAL HIGDON:
        ${getWeeklyDistribution(input.level!!, input.goal!!, dayOfWeek)}
        
        GIỚI HẠN PACE THEO LOẠI BUỔI TẬP VÀ TRÌNH ĐỘ:
        • RECOVERY_RUN:
          ◦ Beginner: 7:30 – 9:00 phút/km
          ◦ Intermediate: 6:30 – 8:00 phút/km
          ◦ Advanced: 5:30 – 7:00 phút/km
        • LONG_RUN:
          ◦ Beginner: 7:00 – 8:30 phút/km
          ◦ Intermediate: 6:00 – 7:30 phút/km
          ◦ Advanced: 5:00 – 6:30 phút/km
        • SPEED_WORK:
          ◦ Beginner: 6:00 – 7:30 phút/km
          ◦ Intermediate: 5:00 – 6:30 phút/km
          ◦ Advanced: 4:00 – 5:30 phút/km
        
        ĐIỀU CHỈNH THEO TIẾN ĐỘ TRAINING:
        • Tuần 1-4: Base building phase (60-70% easy pace)
        • Tuần 5-12: Build phase (Tăng SPEED_WORK và LONG_RUN)
        • Tuần 13-16: Peak phase (Highest volume)
        • Tuần 17-18: Taper phase (Giảm volume, duy trì intensity)
        
        YÊU CẦU ĐẦU RA:
        Trả về CHÍNH XÁC một đối tượng JSON cho ngày tập luyện theo chuẩn Hal Higdon:
        {
          "week": $week,
          "dayOfWeek": $dayOfWeek,
          "session": {
            "name": "[tên buổi tập theo Hal Higdon, ví dụ: 'Long Run', 'Tempo Run', 'Easy Run']",
            "type": "[LONG_RUN | RECOVERY_RUN | SPEED_WORK | REST]",
            "distance": [số km, theo progression Hal Higdon],
            "pace": [phút/km, dựa trên training zones],
            "notes": "[hướng dẫn chi tiết theo phương pháp Hal Higdon]"
          }
        }

        ĐIỀU CHỈNH THEO TRÌNH ĐỘ HAL HIGDON:
        ${getTrainingAdjustmentByLevel(input.level!!, input.goal!!)}

        QUAN TRỌNG:
        • Tuân thủ nghiêm ngặt cấu trúc tuần của Hal Higdon
        • Chỉ trả về một đối tượng JSON cho ngày ${date.toLocalDate()}
        • Sử dụng tiếng Việt
        • Đảm bảo JSON hợp lệ
        • Dựa trên feedback để điều chỉnh nhẹ nhưng không vi phạm nguyên tắc Hal Higdon
    """.trimIndent()
    }

    data class TrainingStandards(
        val totalWeeks: Int,
        val weeklyStructure: String,
        val longestWorkout: String,
        val runDaysPerWeek: Int
    )

    private fun getHalHigdonStandards(goal: ETrainingPlanInputGoal, level: ETrainingPlanInputLevel): TrainingStandards {
        return when (goal) {
            ETrainingPlanInputGoal.MARATHON_FINISH, ETrainingPlanInputGoal.MARATHON_TIME -> {
                when (level) {
                    ETrainingPlanInputLevel.BEGINNER -> TrainingStandards(
                        totalWeeks = 18,
                        weeklyStructure = "2 Day Off, 7 Other, 4 Run, 1 X-Train",
                        longestWorkout = "32 km",
                        runDaysPerWeek = 4
                    )
                    ETrainingPlanInputLevel.INTERMEDIATE -> TrainingStandards(
                        totalWeeks = 18,
                        weeklyStructure = "1 X-Train, 7 Other, 5 Run, 1 Day Off",
                        longestWorkout = "32 km",
                        runDaysPerWeek = 5
                    )
                    ETrainingPlanInputLevel.ADVANCED -> TrainingStandards(
                        totalWeeks = 18,
                        weeklyStructure = "6 Run, 7 Other, 1 Day Off",
                        longestWorkout = "32 km",
                        runDaysPerWeek = 6
                    )
                }
            }
            ETrainingPlanInputGoal.HALF_MARATHON_FINISH, ETrainingPlanInputGoal.HALF_MARATHON_TIME -> {
                when (level) {
                    ETrainingPlanInputLevel.BEGINNER -> TrainingStandards(
                        totalWeeks = 12,
                        weeklyStructure = "2 Day Off, 4 Run, 2 X-Train, 1 Strength",
                        longestWorkout = "16 km",
                        runDaysPerWeek = 4
                    )
                    ETrainingPlanInputLevel.INTERMEDIATE -> TrainingStandards(
                        totalWeeks = 12,
                        weeklyStructure = "1 X-Train, 5 Run, 2 Day Off",
                        longestWorkout = "19 km",
                        runDaysPerWeek = 5
                    )
                    ETrainingPlanInputLevel.ADVANCED -> TrainingStandards(
                        totalWeeks = 12,
                        weeklyStructure = "6 Run, 1 Day Off, 2 Strength",
                        longestWorkout = "2 hours",
                        runDaysPerWeek = 6
                    )
                }
            }
            ETrainingPlanInputGoal.TEN_KM_FINISH, ETrainingPlanInputGoal.TEN_KM_TIME -> {
                when (level) {
                    ETrainingPlanInputLevel.BEGINNER -> TrainingStandards(
                        totalWeeks = 8,
                        weeklyStructure = "2 Day Off, 3 Run, 2 X-Train",
                        longestWorkout = "9 km",
                        runDaysPerWeek = 3
                    )
                    ETrainingPlanInputLevel.INTERMEDIATE -> TrainingStandards(
                        totalWeeks = 8,
                        weeklyStructure = "5 Run, 1 Day Off, 1 X-Train",
                        longestWorkout = "13 km",
                        runDaysPerWeek = 5
                    )
                    ETrainingPlanInputLevel.ADVANCED -> TrainingStandards(
                        totalWeeks = 8,
                        weeklyStructure = "7 Run",
                        longestWorkout = "16 km",
                        runDaysPerWeek = 7
                    )
                }
            }
            ETrainingPlanInputGoal.FIVE_KM_FINISH, ETrainingPlanInputGoal.FIVE_KM_TIME -> {
                when (level) {
                    ETrainingPlanInputLevel.BEGINNER -> TrainingStandards(
                        totalWeeks = 8,
                        weeklyStructure = "3 Run, 3 Day Walk, 1 Day Off",
                        longestWorkout = "5 km",
                        runDaysPerWeek = 3
                    )
                    ETrainingPlanInputLevel.INTERMEDIATE -> TrainingStandards(
                        totalWeeks = 8,
                        weeklyStructure = "5 Run, 2 Day Off",
                        longestWorkout = "11 km",
                        runDaysPerWeek = 5
                    )
                    ETrainingPlanInputLevel.ADVANCED -> TrainingStandards(
                        totalWeeks = 8,
                        weeklyStructure = "6 Run, 1 Day Off",
                        longestWorkout = "19 km",
                        runDaysPerWeek = 6
                    )
                }
            }
            else -> TrainingStandards(
                totalWeeks = 12,
                weeklyStructure = "4 Run, 1 Walk, 2 Days Off",
                longestWorkout = "10 km",
                runDaysPerWeek = 4
            )
        }
    }

    private fun getSpeedWorkGuidance(level: ETrainingPlanInputLevel, goal: ETrainingPlanInputGoal): String {
        return when (level) {
            ETrainingPlanInputLevel.BEGINNER -> "Tempo runs 1x/tuần, không quá 20% tổng volume"
            ETrainingPlanInputLevel.INTERMEDIATE -> "Tempo + Intervals, 2x/tuần, khoảng 25% tổng volume"
            ETrainingPlanInputLevel.ADVANCED -> "Variety speed work, 2-3x/tuần, có thể lên đến 30% tổng volume"
        }
    }

    private fun getWeeklyDistribution(level: ETrainingPlanInputLevel, goal: ETrainingPlanInputGoal, dayOfWeek: Int): String {
        val marathonDistribution = when (level) {
            ETrainingPlanInputLevel.BEGINNER -> """
        • Thứ 2: REST
        • Thứ 3: Easy Run (5-8km)
        • Thứ 4: Cross Training hoặc REST
        • Thứ 5: Easy Run (5-8km)
        • Thứ 6: REST
        • Thứ 7: Easy Run (8-13km)
        • Chủ nhật: Long Run (16-32km)
        """
            ETrainingPlanInputLevel.INTERMEDIATE -> """
        • Thứ 2: Easy Run (6-10km)
        • Thứ 3: Tempo/Speed Work
        • Thứ 4: Easy Run (6-10km)
        • Thứ 5: Tempo/Speed Work
        • Thứ 6: REST hoặc Cross Training
        • Thứ 7: Medium Run (10-16km)
        • Chủ nhật: Long Run (19-32km)
        """
            ETrainingPlanInputLevel.ADVANCED -> """
        • Thứ 2: Easy Run (8-13km)
        • Thứ 3: Speed Work/Intervals
        • Thứ 4: Easy Run (8-13km)
        • Thứ 5: Tempo Run
        • Thứ 6: Easy Run (6-10km)
        • Thứ 7: Medium Long Run (16-24km)
        • Chủ nhật: Long Run (24-32km)
        """
        }

        return when (goal) {
            ETrainingPlanInputGoal.MARATHON_FINISH, ETrainingPlanInputGoal.MARATHON_TIME -> marathonDistribution
            else -> "Điều chỉnh theo mục tiêu cụ thể với volume thấp hơn"
        }
    }

    private fun getTrainingAdjustmentByLevel(level: ETrainingPlanInputLevel, goal: ETrainingPlanInputGoal): String {
        return when (level) {
            ETrainingPlanInputLevel.BEGINNER -> """
        • Ưu tiên hoàn thành khoảng cách hơn tốc độ
        • Long run tăng không quá 1.6km mỗi tuần
        • Chỉ 1 buổi speed work/tuần
        • Nhiều ngày nghỉ để phục hồi
        """
            ETrainingPlanInputLevel.INTERMEDIATE -> """
        • Cân bằng volume và intensity
        • 2 buổi quality runs/tuần (Tempo + Speed)
        • Long run có thể kết hợp pace changes
        • 1 ngày nghỉ hoàn toàn/tuần
        """
            ETrainingPlanInputLevel.ADVANCED -> """
        • High volume với multiple quality sessions
        • 2-3 buổi speed work/tuần
        • Long runs với marathon pace segments
        • Có thể double runs trong ngày
        """
        }
    }


    private fun parseDailyAIResponse(aiResponse: String, plan: TrainingPlan, date: LocalDateTime): TrainingDay {
        try {
            val jsonPattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL)
            val matcher = jsonPattern.matcher(aiResponse)

            if (matcher.find()) {
                val dayJson = JSONObject(matcher.group())
                val sessionJson = dayJson.getJSONObject("session")
                val week = dayJson.getInt("week")
                val dayOfWeek = dayJson.getInt("dayOfWeek")

                val session = TrainingSession(
                    name = sessionJson.getString("name"),
                    type = ETrainingSessionType.valueOf(sessionJson.getString("type")),
                    distance = sessionJson.getDouble("distance"),
                    pace = sessionJson.getDouble("pace"),
                    notes = if (sessionJson.has("notes")) sessionJson.getString("notes") else null
                )

                val savedSession = trainingSessionRepository.save(session)

                val trainingDay = TrainingDay().apply {
                    this.plan = plan
                    this.session = savedSession
                    this.week = week
                    this.dayOfWeek = dayOfWeek
                    this.record = null
                    this.status = ETrainingDayStatus.ACTIVE
                    this.dateTime = date
                }

                savedSession.trainingDays = savedSession.trainingDays.toMutableList().apply {
                    add(trainingDay)
                }

                return trainingDay
            } else {
                return createDefaultTrainingDay(plan, date)
            }
        } catch (e: Exception) {
            println("Error parsing AI response for daily training: ${e.message}")
            return createDefaultTrainingDay(plan, date)
        }
    }

    private fun createDefaultTrainingDay(plan: TrainingPlan, date: LocalDateTime): TrainingDay {
        val week = java.time.temporal.ChronoUnit.DAYS.between(plan.startDate, date).div(7) + 1
        val dayOfWeek = date.dayOfWeek.value

        val restSession = TrainingSession(
            name = "Nghỉ ngơi",
            type = ETrainingSessionType.REST,
            distance = 0.0,
            pace = 0.0,
            notes = "Ngày nghỉ phục hồi"
        )

        val savedRestSession = trainingSessionRepository.save(restSession)

        return TrainingDay().apply {
            this.plan = plan
            this.session = savedRestSession
            this.week = week.toInt()
            this.dayOfWeek = dayOfWeek
            this.record = null
            this.status = ETrainingDayStatus.ACTIVE
            this.dateTime = date
        }
    }

    private fun callAIApi(prompt: String): String {
        val client = OkHttpClient()

        val messageObj = JSONObject()
            .put("role", "user")
            .put("content", prompt)

        val jsonBody = JSONObject()
            .put("model", GROQ_MODEL)
            .put("messages", JSONArray().put(messageObj))
            .put("temperature", 0.7)

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            return jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }



    // Extension function để làm tròn số thập phân
    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }


    fun calculateDateTimeForTrainingDay(planStartDate: LocalDateTime, week: Int, dayOfWeek: Int): LocalDateTime {
        // (week - 1) * 7 + (dayOfWeek - 1) để tính tổng số ngày từ planStartDate
        val daysToAdd = (week - 1) * 7 + (dayOfWeek - 1)
        return planStartDate.plusDays(daysToAdd.toLong())
    }
}