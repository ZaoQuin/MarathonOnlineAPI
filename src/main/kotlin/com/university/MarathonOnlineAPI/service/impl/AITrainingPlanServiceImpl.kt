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

    private val apiKey = aiTrainingProperties.api
    private val GROQ_MODEL = "llama3-70b-8192"

    data class PaceConstraints(
        val recoveryRunMin: Double,
        val recoveryRunMax: Double,
        val longRunMin: Double,
        val longRunMax: Double,
        val speedWorkMin: Double,
        val speedWorkMax: Double
    )

    override fun generateTrainingDayForDate(input: TrainingPlanInput, plan: TrainingPlan, date: LocalDateTime): TrainingDay {
        val previousDays = trainingDayRepository.findByPlanIdAndDateTimeBefore(plan.id!!, date)
        val feedbackData = previousDays.mapNotNull { it.trainingFeedback }
        val recordData: List<Record> = previousDays.mapNotNull { it.record }

        val daysSinceStart = ChronoUnit.DAYS.between(plan.startDate!!.toLocalDate(), date.toLocalDate())
        val week = (daysSinceStart / 7 + 1).toInt()
        val dayOfWeek = (daysSinceStart % 7 + 1).toInt()

        val prompt = createPromptForDailyTraining(input, plan, date, feedbackData, recordData, week, dayOfWeek)
        val aiResponse = callAIApi(prompt)
        val trainingDay = parseDailyAIResponse(aiResponse, plan, date, input.level!!, week, dayOfWeek)

        return trainingDayRepository.save(trainingDay)
    }


    private fun getPaceConstraints(level: ETrainingPlanInputLevel): PaceConstraints {
        return when (level) {
            ETrainingPlanInputLevel.BEGINNER -> PaceConstraints(
                recoveryRunMin = 7.5, recoveryRunMax = 9.0,
                longRunMin = 7.0, longRunMax = 8.5,
                speedWorkMin = 6.0, speedWorkMax = 7.5
            )
            ETrainingPlanInputLevel.INTERMEDIATE -> PaceConstraints(
                recoveryRunMin = 6.5, recoveryRunMax = 8.0,
                longRunMin = 6.0, longRunMax = 7.5,
                speedWorkMin = 5.0, speedWorkMax = 6.5
            )
            ETrainingPlanInputLevel.ADVANCED -> PaceConstraints(
                recoveryRunMin = 5.5, recoveryRunMax = 7.0,
                longRunMin = 5.0, longRunMax = 6.5,
                speedWorkMin = 4.0, speedWorkMax = 5.5
            )
        }
    }

    private fun validateAndAdjustPace(
        sessionType: ETrainingSessionType,
        proposedPace: Double,
        level: ETrainingPlanInputLevel
    ): Double {
        val constraints = getPaceConstraints(level)

        return when (sessionType) {
            ETrainingSessionType.RECOVERY_RUN -> {
                proposedPace.coerceIn(constraints.recoveryRunMin, constraints.recoveryRunMax)
            }
            ETrainingSessionType.LONG_RUN -> {
                proposedPace.coerceIn(constraints.longRunMin, constraints.longRunMax)
            }
            ETrainingSessionType.SPEED_WORK -> {
                proposedPace.coerceIn(constraints.speedWorkMin, constraints.speedWorkMax)
            }
            ETrainingSessionType.REST -> 0.0
        }
    }

    private fun createPromptForDailyTraining(
        input: TrainingPlanInput,
        plan: TrainingPlan,
        date: LocalDateTime,
        feedbackData: List<TrainingFeedback>,
        recordData: List<Record>,
        week: Int,
        dayOfWeek: Int
    ): String {
        val recentRecords = recordData.takeLast(5)
        val recentFeedbacks = feedbackData.takeLast(5)
        val performanceTrend = analyzePerformanceTrend(recentRecords, recentFeedbacks)
        val complianceRate = calculateComplianceRate(trainingDayRepository.findByPlanId(plan.id!!))
        val missedSessions = trainingDayRepository.findByPlanId(plan.id!!).count { it.status == ETrainingDayStatus.MISSED }

        val avgHeartRate = recentRecords.mapNotNull { it.heartRate }.average().let { if (it.isNaN()) 0.0 else it.round(2) }
        val feedbackNotesSummary = recentFeedbacks.mapNotNull { it.notes }.joinToString("; ") { it.take(100) }
        val avgDifficulty = recentFeedbacks.map { it.difficultyRating }.groupingBy { it }.eachCount()
        val avgFeeling = recentFeedbacks.map { it.feelingRating }.groupingBy { it }.eachCount()
        val difficultySummary = avgDifficulty.entries.joinToString { "${it.key}: ${it.value}" }
        val feelingSummary = avgFeeling.entries.joinToString { "${it.key}: ${it.value}" }
        val avgDistance = recentRecords.mapNotNull { it.distance }.average().let { if (it.isNaN()) 0.0 else it.round(2) }
        val avgPace = recentRecords.mapNotNull { it.avgSpeed }.average().let { if (it.isNaN()) 0.0 else it.round(2) }

        val trainingStandards = getHalHigdonStandards(input.goal!!, input.level!!)
        val paceConstraints = getPaceConstraints(input.level!!)

        return """
    Tạo một ngày tập luyện marathon được cá nhân hóa cho ngày ${date.toLocalDate()} dựa trên phương pháp Hal Higdon và thông tin sau:
    
    THÔNG TIN RUNNER:
    • Trình độ: ${input.level}
    • Mục tiêu: ${input.goal}
    • Khoảng cách dài nhất từng chạy: ${input.maxDistance ?: 10.0} km
    • Tốc độ trung bình: ${input.averagePace ?: 6.0} phút/km
    • Số tuần luyện tập: ${input.trainingWeeks ?: trainingStandards.totalWeeks}
    • Tuần hiện tại: $week/${trainingStandards.totalWeeks}
    • Ngày trong tuần tập luyện: $dayOfWeek/7 (ngày $dayOfWeek của tuần $week)
    
    RÀNG BUỘC PACE THEO TRÌNH ĐỘ ${input.level}:
    • Recovery Run: ${paceConstraints.recoveryRunMin} - ${paceConstraints.recoveryRunMax} phút/km
    • Long Run: ${paceConstraints.longRunMin} - ${paceConstraints.longRunMax} phút/km
    • Speed Work: ${paceConstraints.speedWorkMin} - ${paceConstraints.speedWorkMax} phút/km
    
    CHUẨN HAL HIGDON CHO ${input.goal} - ${input.level}:
    • Tổng thời gian: ${trainingStandards.totalWeeks} tuần
    • Tuần điển hình: ${trainingStandards.weeklyStructure}
    • Longest Workout: ${trainingStandards.longestWorkout}
    • Số ngày chạy/tuần: ${trainingStandards.runDaysPerWeek}
    
    DỮ LIỆU TỪ 5 BUỔI TẬP GẦN NHẤT:
    • Độ khó trung bình: $difficultySummary
    • Cảm giác trung bình: $feelingSummary
    • Khoảng cách trung bình: $avgDistance km
    • Tốc độ trung bình: $avgPace phút/km
    • Nhịp tim trung bình: $avgHeartRate bpm
    • Xu hướng hiệu suất: $performanceTrend
    • Tỷ lệ tuân thủ: $complianceRate%
    • Số buổi bị bỏ lỡ: $missedSessions
    • Ghi chú từ feedback: $feedbackNotesSummary
    
    HƯỚNG DẪN ĐIỀU CHỈNH:
    • **BẮT BUỘC tuân thủ ràng buộc pace theo trình độ ở trên**
    • Nếu có buổi MISSED (>= 2 buổi trong 5 buổi gần nhất) hoặc tỷ lệ tuân thủ <70%,
      hãy chọn pace ở mức cao hơn trong phạm vi cho phép và ưu tiên RECOVERY_RUN hoặc REST.
    • Nếu cảm giác TIRED hoặc EXHAUSTED chiếm >50% hoặc xu hướng hiệu suất là "Giảm sút",
      chọn pace ở mức trung bình cao trong phạm vi cho phép
    • Nếu ghi chú nhắc đến thời tiết xấu hoặc chấn thương,
      **bắt buộc chọn RECOVERY_RUN hoặc REST** với pace ở mức cao nhất trong phạm vi cho phép.
    • **Tuyệt đối không được tạo pace nằm ngoài phạm vi quy định cho từng loại buổi tập.**

    QUAN TRỌNG: Trả lời hoàn toàn bằng TIẾNG VIỆT. Trả về CHÍNH XÁC một đối tượng JSON hợp lệ sau đây, không có thêm văn bản hay định dạng nào khác:
    
    {
      "week": $week,
      "dayOfWeek": $dayOfWeek,
      "session": {
        "name": "tên buổi tập theo Hal Higdon",
        "type": "LONG_RUN hoặc RECOVERY_RUN hoặc SPEED_WORK hoặc REST",
        "distance": số_km_dạng_số,
        "pace": số_phút_per_km_dạng_số_trong_phạm_vi_cho_phép,
        "notes": "hướng dẫn chi tiết cho người chạy"
      }
    }
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

    private fun parseDailyAIResponse(
        aiResponse: String,
        plan: TrainingPlan,
        date: LocalDateTime,
        level: ETrainingPlanInputLevel,
        calculatedWeek: Int,
        calculatedDayOfWeek: Int
    ): TrainingDay {
        return try {
            val cleanedResponse = aiResponse.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim()

            // More robust JSON extraction
            val jsonPattern = Pattern.compile("\\{.*?\"session\".*?\\{.*?\\}.*?\\}", Pattern.DOTALL)
            val matcher = jsonPattern.matcher(cleanedResponse)

            val jsonString = if (matcher.find()) {
                matcher.group()
            } else {
                // Fallback to simple extraction
                val startIndex = cleanedResponse.indexOf('{')
                val endIndex = cleanedResponse.lastIndexOf('}')
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    cleanedResponse.substring(startIndex, endIndex + 1)
                } else {
                    throw IllegalArgumentException("No valid JSON found in response")
                }
            }

            println("Attempting to parse JSON: $jsonString")

            val dayJson = JSONObject(jsonString)
            val sessionJson = dayJson.getJSONObject("session")

            // Use calculated values instead of AI response values to ensure consistency
            val week = calculatedWeek
            val dayOfWeek = calculatedDayOfWeek

            val sessionType = try {
                ETrainingSessionType.valueOf(sessionJson.getString("type"))
            } catch (e: IllegalArgumentException) {
                println("Invalid session type: ${sessionJson.optString("type", "UNKNOWN")}, defaulting to RECOVERY_RUN")
                ETrainingSessionType.RECOVERY_RUN
            }

            // Validate and adjust pace according to constraints
            val aiPace = sessionJson.optDouble("pace", 7.0)
            val validatedPace = validateAndAdjustPace(sessionType, aiPace, level)

            // Log pace adjustment if needed
            if (aiPace != validatedPace) {
                println("Pace adjusted from $aiPace to $validatedPace for session type $sessionType and level $level")
            }

            val session = TrainingSession(
                name = sessionJson.optString("name", "Training Session"),
                type = sessionType,
                distance = sessionJson.optDouble("distance", 5.0),
                pace = validatedPace,
                notes = sessionJson.optString("notes", "") +
                        if (aiPace != validatedPace) " (Pace đã được điều chỉnh theo quy định: $validatedPace phút/km)" else ""
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
                this.completionPercentage = 0.0
            }

            savedSession.trainingDays = savedSession.trainingDays.toMutableList().apply {
                add(trainingDay)
            }

            trainingDay
        } catch (e: Exception) {
            println("Error parsing AI response for daily training: ${e.message}")
            println("AI Response was: $aiResponse")
            e.printStackTrace()
            createDefaultTrainingDay(plan, date, level, calculatedWeek, calculatedDayOfWeek)
        }
    }

    private fun createDefaultTrainingDay(
        plan: TrainingPlan,
        date: LocalDateTime,
        level: ETrainingPlanInputLevel,
        week: Int,
        dayOfWeek: Int
    ): TrainingDay {
        val paceConstraints = getPaceConstraints(level)

        val restSession = TrainingSession(
            name = "Nghỉ ngơi",
            type = ETrainingSessionType.REST,
            distance = 0.0,
            pace = 0.0,
            notes = "Ngày nghỉ phục hồi - Tuần $week, Ngày $dayOfWeek - Pace constraints được áp dụng: Recovery ${paceConstraints.recoveryRunMin}-${paceConstraints.recoveryRunMax}, Long ${paceConstraints.longRunMin}-${paceConstraints.longRunMax}, Speed ${paceConstraints.speedWorkMin}-${paceConstraints.speedWorkMax} phút/km"
        )

        val savedRestSession = trainingSessionRepository.save(restSession)

        return TrainingDay().apply {
            this.plan = plan
            this.session = savedRestSession
            this.week = week
            this.dayOfWeek = dayOfWeek
            this.record = null
            this.status = ETrainingDayStatus.ACTIVE
            this.dateTime = date
            this.completionPercentage = 0.0
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
            .put("temperature", 0.1) // Even lower temperature for more consistent JSON
            .put("max_tokens", 300) // Reduced tokens for focused response
            .put("response_format", JSONObject().put("type", "json_object")) // Request JSON format

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("API call failed with code: ${response.code}")
                    println("Response body: ${response.body?.string()}")
                    throw IOException("Unexpected code $response")
                }

                val responseBody = response.body?.string() ?: ""
                println("Raw API Response: $responseBody")

                val jsonResponse = JSONObject(responseBody)
                val content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                println("AI API Response Content: $content")
                content
            }
        } catch (e: Exception) {
            println("Error calling AI API: ${e.message}")
            e.printStackTrace()
            // Return a valid JSON fallback
            """
            {
              "week": 1,
              "dayOfWeek": 1,
              "session": {
                "name": "Easy Run",
                "type": "RECOVERY_RUN",
                "distance": 5.0,
                "pace": 7.5,
                "notes": "Chạy nhẹ nhàng để khởi động"
              }
            }
            """.trimIndent()
        }
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    private fun analyzePerformanceTrend(records: List<Record>, feedbacks: List<TrainingFeedback>): String {
        if (records.isEmpty()) return "Không đủ dữ liệu"

        val distances = records.mapNotNull { it.distance }
        val paces = records.mapNotNull { it.avgSpeed }
        val difficulties = feedbacks.map { it.difficultyRating }

        val distanceTrend = if (distances.size >= 2) {
            val recentAvg = distances.takeLast(3).average()
            val olderAvg = distances.take(3).average()
            when {
                recentAvg > olderAvg * 1.1 -> "Cải thiện"
                recentAvg < olderAvg * 0.9 -> "Giảm sút"
                else -> "Ổn định"
            }
        } else "Không đủ dữ liệu"

        val paceTrend = if (paces.size >= 2) {
            val recentAvg = paces.takeLast(3).average()
            val olderAvg = paces.take(3).average()
            when {
                recentAvg < olderAvg * 0.9 -> "Cải thiện"
                recentAvg > olderAvg * 1.1 -> "Giảm sút"
                else -> "Ổn định"
            }
        } else "Không đủ dữ liệu"

        val difficultyTrend = if (difficulties.size >= 2) {
            val hardCount = difficulties.count { it in listOf(EDifficultyRating.HARD, EDifficultyRating.VERY_HARD) }
            when {
                hardCount > difficulties.size / 2 -> "Quá sức"
                hardCount < difficulties.size / 4 -> "Dễ dàng"
                else -> "Phù hợp"
            }
        } else "Không đủ dữ liệu"

        return "Khoảng cách: $distanceTrend, Tốc độ: $paceTrend, Độ khó: $difficultyTrend"
    }

    private fun calculateComplianceRate(trainingDays: List<TrainingDay>): Double {
        if (trainingDays.isEmpty()) return 100.0
        val completedDays = trainingDays.count { it.status in listOf(ETrainingDayStatus.COMPLETED, ETrainingDayStatus.PARTIALLY_COMPLETED) }
        return (completedDays.toDouble() / trainingDays.size * 100).round(2)
    }
}