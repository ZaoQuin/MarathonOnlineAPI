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

        return """
            Tạo một ngày tập luyện marathon được cá nhân hóa cho ngày ${date.toLocalDate()} dựa trên thông tin sau:
            
            THÔNG TIN RUNNER:
            • Trình độ: ${input.level}
            • Mục tiêu: ${input.goal}
            • Khoảng cách dài nhất từng chạy: ${input.maxDistance ?: 10.0} km
            • Tốc độ trung bình: ${input.averagePace ?: 6.0} phút/km
            • Số tuần luyện tập: ${input.trainingWeeks ?: 4}
            • Tuần hiện tại: $week
            • Ngày trong tuần: $dayOfWeek (1=Thứ Hai, 7=Chủ Nhật)
            
            DỮ LIỆU TỪ CÁC BUỔI TẬP TRƯỚC:
            • Độ khó trung bình: $difficultySummary
            • Cảm giác trung bình: $feelingSummary
            • Khoảng cách trung bình: ${avgDistance.round(2)} km
            • Tốc độ trung bình: ${avgPace.round(2)} phút/km
            
            NGUYÊN TẮC HUẤN LUYỆN:
            • Mỗi tuần có 4-5 ngày tập, ưu tiên phân bố đều
            • LONG_RUN: Thường vào cuối tuần (ngày 6 hoặc 7), tăng dần 10-15% qua các tuần
            • SPEED_WORK: 1-2 buổi/tuần (ngày 2 hoặc 4), không liên tiếp với LONG_RUN
            • RECOVERY_RUN: Pace chậm hơn mục tiêu 30-60 giây/km
            • Nếu feedback báo TIRED/EXHAUSTED hoặc độ khó HARD/VERY_HARD, giảm khoảng cách 20% hoặc chuyển sang RECOVERY_RUN
            
            GIỚI HẠN PACE THEO LOẠI BUỔI TẬP VÀ TRÌNH ĐỘ (NGUỒN: Runna):
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
            
            YÊU CẦU ĐẦU RA:
            Trả về CHÍNH XÁC một đối tượng JSON cho ngày tập luyện:
            {
              "week": $week,
              "dayOfWeek": $dayOfWeek,
              "session": {
                "name": "[tên buổi tập, ví dụ: 'Tempo Run Tuần $week']",
                "type": "[LONG_RUN | RECOVERY_RUN | SPEED_WORK | REST]",
                "distance": [số km, làm tròn 1 chữ số thập phân],
                "pace": [phút/km, làm tròn 1 chữ số thập phân],
                "notes": "[hướng dẫn chi tiết cho buổi tập, ví dụ: 'Chạy đều, duy trì nhịp thở ổn định. Uống nước mỗi 5km.' hoặc 'Chạy 4x800m ở pace mục tiêu với 2 phút nghỉ giữa các lần.' for SPEED_WORK]"
              }
            }

            ĐIỀU CHỈNH THEO TRÌNH ĐỘ:
            ${
                when (input.level) {
                    ETrainingPlanInputLevel.BEGINNER -> "Ưu tiên sức bền, LONG_RUN không quá 50% maxDistance, pace gần giới hạn trên"
                    ETrainingPlanInputLevel.INTERMEDIATE -> "Cân bằng SPEED_WORK (10-15% khối lượng tuần) và RECOVERY_RUN"
                    else -> "Tăng cường SPEED_WORK (15-20% khối lượng tuần) với Tempo và Yasso 800s"
                }
            }
    
            ĐIỀU CHỈNH THEO MỤC TIÊU:
            ${
                when (input.goal) {
                    ETrainingPlanInputGoal.MARATHON_FINISH -> "Tập trung LONG_RUN (50-65% khối lượng tuần) và sức bền"
                    ETrainingPlanInputGoal.MARATHON_TIME -> "Tăng SPEED_WORK (15-20% khối lượng tuần) với Tempo và Yasso 800s"
                    else -> "Cân bằng sức bền (LONG_RUN 40-50% tuần) và tốc độ (SPEED_WORK 10-15% tuần)"
                }
            }
    
        QUAN TRỌNG:
        • Chỉ trả về một đối tượng JSON cho ngày ${date.toLocalDate()}
        • Sử dụng tiếng Việt
        • Đảm bảo JSON hợp lệ
        • Dựa trên dữ liệu lịch sử, điều chỉnh pace/distance nếu cần để tránh quá tải
        """.trimIndent()
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
                // Tạo ngày nghỉ mặc định nếu không parse được
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