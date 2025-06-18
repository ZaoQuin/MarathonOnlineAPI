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
        
        QUAN TRỌNG: Trả về CHÍNH XÁC một đối tượng JSON hợp lệ sau đây, không có thêm text nào khác:
        
        {
          "week": $week,
          "dayOfWeek": $dayOfWeek,
          "session": {
            "name": "tên buổi tập theo Hal Higdon",
            "type": "LONG_RUN hoặc RECOVERY_RUN hoặc SPEED_WORK hoặc REST",
            "distance": số_km_dạng_số,
            "pace": số_phút_per_km_dạng_số,
            "notes": "hướng dẫn chi tiết"
          }
        }
        
        Chỉ trả về JSON, không có markdown, không có text giải thích thêm.
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

    private fun parseDailyAIResponse(aiResponse: String, plan: TrainingPlan, date: LocalDateTime): TrainingDay {
        return try {
            // Clean the response to remove any markdown formatting or extra text
            val cleanedResponse = aiResponse.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim()

            // Try to find JSON pattern
            val jsonPattern = Pattern.compile("\\{[^{}]*\\{[^{}]*\\}[^{}]*\\}", Pattern.DOTALL)
            val matcher = jsonPattern.matcher(cleanedResponse)

            val jsonString = if (matcher.find()) {
                matcher.group()
            } else {
                // If no nested JSON found, try to find any JSON object
                val simpleJsonPattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL)
                val simpleMatcher = simpleJsonPattern.matcher(cleanedResponse)
                if (simpleMatcher.find()) {
                    simpleMatcher.group()
                } else {
                    cleanedResponse
                }
            }

            println("Attempting to parse JSON: $jsonString")

            val dayJson = JSONObject(jsonString)
            val sessionJson = dayJson.getJSONObject("session")
            val week = dayJson.getInt("week")
            val dayOfWeek = dayJson.getInt("dayOfWeek")

            // Safely parse session type
            val sessionType = try {
                ETrainingSessionType.valueOf(sessionJson.getString("type"))
            } catch (e: IllegalArgumentException) {
                println("Invalid session type: ${sessionJson.getString("type")}, defaulting to RECOVERY_RUN")
                ETrainingSessionType.RECOVERY_RUN
            }

            val session = TrainingSession(
                name = sessionJson.getString("name"),
                type = sessionType,
                distance = sessionJson.getDouble("distance"),
                pace = sessionJson.getDouble("pace"),
                notes = sessionJson.optString("notes", "")
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

            trainingDay

        } catch (e: Exception) {
            println("Error parsing AI response for daily training: ${e.message}")
            println("AI Response was: $aiResponse")
            createDefaultTrainingDay(plan, date)
        }
    }

    private fun createDefaultTrainingDay(plan: TrainingPlan, date: LocalDateTime): TrainingDay {
        val week = ChronoUnit.DAYS.between(plan.startDate, date).div(7) + 1
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
            .put("temperature", 0.3) // Lower temperature for more consistent JSON output
            .put("max_tokens", 500) // Limit tokens to ensure focused response

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
                    throw IOException("Unexpected code $response")
                }

                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                println("AI API Response: $content")
                content
            }
        } catch (e: Exception) {
            println("Error calling AI API: ${e.message}")
            // Return a default JSON response
            """
            {
              "week": 1,
              "dayOfWeek": 1,
              "session": {
                "name": "Easy Run",
                "type": "RECOVERY_RUN",
                "distance": 5.0,
                "pace": 7.0,
                "notes": "Chạy nhẹ nhàng để khởi động"
              }
            }
            """.trimIndent()
        }
    }

    // Extension function để làm tròn số thập phân
    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    fun calculateDateTimeForTrainingDay(planStartDate: LocalDateTime, week: Int, dayOfWeek: Int): LocalDateTime {
        val daysToAdd = (week - 1) * 7 + (dayOfWeek - 1)
        return planStartDate.plusDays(daysToAdd.toLong())
    }
}