package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.repos.*
import com.university.MarathonOnlineAPI.service.AITrainingPlanService
import io.github.cdimascio.dotenv.Dotenv
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.regex.Pattern

@Service
class AITrainingPlanServiceImpl(
    private val trainingSessionRepository: TrainingSessionRepository,
    private val trainingDayRepository: TrainingDayRepository,
    private val userRepository: UserRepository
): AITrainingPlanService {
    val dotenv = Dotenv.load()!!
    val apiKey = dotenv["GROQ_API_KEY"]!!
    private val GROQ_MODEL = "llama3-70b-8192"

    override fun generateTrainingDays(input: TrainingPlanInput, plan: TrainingPlan): List<TrainingDay> {
        // Tạo prompt cho AI dựa trên thông tin đầu vào
        val prompt = createPromptForAI(input)

        // Gọi API AI để tạo lịch trình
        val aiResponse = callAIApi(prompt)

        // Xử lý phản hồi từ AI
        val trainingDays = parseAIResponse(aiResponse, plan)

        // Lưu tất cả training days
        return trainingDayRepository.saveAll(trainingDays)
    }

    private fun createPromptForAI(input: TrainingPlanInput): String {
        return """
        Hãy tạo một lịch trình luyện tập marathon chi tiết dưới dạng JSON dựa trên thông tin của runner sau:

        • Trình độ: ${input.level}
        • Mục tiêu: ${input.goal}
        • Khoảng cách dài nhất từng chạy: ${input.maxDistance} km
        • Tốc độ trung bình: ${input.averagePace} phút/km
        • Tổng số tuần luyện tập: ${input.weeks}
        • Số buổi tập mỗi tuần: ${input.daysPerWeek}

        ✳️ **YÊU CẦU ĐẦU RA**:
        Trả về một **mảng JSON hợp lệ**, KHÔNG thêm văn bản giải thích. Mỗi phần tử trong mảng tương ứng với một ngày trong lịch trình với các trường là bắt buộc, có định dạng:

        {
          "week": [số tuần, từ 1 đến ${input.weeks}],
          "dayOfWeek": [ngày trong tuần, từ 1 (Thứ Hai) đến 7 (Chủ Nhật)],
          "session": {
            "name": "[tên buổi tập]",
            "type": "[LONG_RUN | RECOVERY_RUN | SPEED_WORK | REST]",
            "distance": [số km, số thập phân, dùng 0 nếu REST],
            "pace": [phút/km, số thập phân, dùng 0 nếu REST],
            "notes": "Nhắc nhở ngắn tùy thuộc loại hình tập luyện, ví dụ: 'Giữ pace ổn định toàn buổi', "
          }
        }

        📌 **Quan trọng**:
        • Mỗi **tuần phải có đúng 7 ngày** (tương ứng 7 phần tử có dayOfWeek từ 1 đến 7).
        • Nếu chỉ tập luyện ${input.daysPerWeek} ngày/tuần, thì các ngày còn lại phải được đánh dấu là `"REST"`.

        🧠 **Nguyên tắc xây dựng kế hoạch**:
        1. LONG_RUN tăng dần qua từng tuần, bắt đầu từ ${(input.maxDistance ?: 10.0).coerceAtLeast(5.0)} km.
        2. SPEED_WORK cần đa dạng (fartlek, interval, tempo, hill repeats).
        3. RECOVERY_RUN nên chậm hơn pace trung bình 1–2 phút/km.
        4. Tránh xếp `"LONG_RUN"` và `"SPEED_WORK"` vào hai ngày liên tiếp.

        📊 **Số phần tử trong mảng đầu ra**:
        ${input.weeks} tuần × 7 ngày = ${input.weeks?.times(7)} phần tử.

        👉 Nhắc lại: **Chỉ trả về mảng JSON hợp lệ, đầy đủ 7 ngày mỗi tuần. Không thêm văn bản mô tả nào khác**.

    """.trimIndent()
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
            println("AI raw response: $responseBody")

            val jsonResponse = JSONObject(responseBody)
            return jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }


    private fun parseAIResponse(aiResponse: String, plan: TrainingPlan): List<TrainingDay> {
        val trainingDays = mutableListOf<TrainingDay>()

        try {
            // Tìm và trích xuất phần JSON từ phản hồi
            val jsonPattern = Pattern.compile("\\[\\s*\\{.*\\}\\s*\\]", Pattern.DOTALL)
            val matcher = jsonPattern.matcher(aiResponse)

            if (matcher.find()) {
                val jsonArray = JSONArray(matcher.group())

                for (i in 0 until jsonArray.length()) {
                    val dayJson = jsonArray.getJSONObject(i)
                    val sessionJson = dayJson.getJSONObject("session")

                    // Tạo hoặc tìm session
                    val session = TrainingSession(
                        name = sessionJson.getString("name"),
                        type = ETrainingSessionType.valueOf(sessionJson.getString("type")),
                        distance = sessionJson.getDouble("distance"),
                        pace = sessionJson.getDouble("pace"),
                        notes = if (sessionJson.has("notes")) sessionJson.getString("notes") else null
                    )

                    val savedSession = trainingSessionRepository.save(session)

                    // Tạo training day
                    val trainingDay = TrainingDay().apply {
                        this.plan = plan
                        this.session = savedSession
                        this.week = dayJson.getInt("week")
                        this.dayOfWeek = dayJson.getInt("dayOfWeek")
                    }

                    trainingDays.add(trainingDay)
                }
            } else {
                // Nếu không tìm thấy JSON, tạo lịch trình mặc định
                trainingDays.addAll(createDefaultTrainingPlan(plan))
            }
        } catch (e: Exception) {
            // Log lỗi và tạo lịch trình mặc định
            println("Error parsing AI response: ${e.message}")
            e.printStackTrace()
            trainingDays.addAll(createDefaultTrainingPlan(plan))
        }

        return trainingDays
    }

    private fun createDefaultTrainingPlan(plan: TrainingPlan): List<TrainingDay> {
        val trainingDays = mutableListOf<TrainingDay>()
        val weeks = plan.input.weeks ?: 12
        val daysPerWeek = plan.input.daysPerWeek ?: 4

        for (week in 1..weeks) {
            // Dựa trên số ngày tập luyện mỗi tuần, phân bổ các ngày tập luyện
            val restDays = 7 - daysPerWeek
            val trainingDaysInWeek = (1..7).filter { it > restDays }.toList()

            for (day in trainingDaysInWeek) {
                // Xác định loại buổi tập dựa trên ngày trong tuần
                val sessionType = when {
                    day == 7 -> ETrainingSessionType.LONG_RUN    // Chủ nhật = chạy dài
                    day % 2 == 0 -> ETrainingSessionType.SPEED_WORK // Ngày chẵn = tập tốc độ
                    else -> ETrainingSessionType.RECOVERY_RUN    // Ngày lẻ = chạy hồi phục
                }

                // Tính khoảng cách dựa trên tuần và loại buổi tập
                val baseDistance = when (sessionType) {
                    ETrainingSessionType.LONG_RUN -> 5.0 + (week * 2.5) // Tăng dần lên 35km
                    ETrainingSessionType.SPEED_WORK -> 5.0
                    ETrainingSessionType.RECOVERY_RUN -> 3.0 + (week * 0.5).coerceAtMost(7.0)
                    else -> 0.0
                }

                // Điều chỉnh khoảng cách cho tuần cuối (marathon)
                val distance = if (week == weeks && sessionType == ETrainingSessionType.LONG_RUN) {
                    42.195 // Marathon đầy đủ
                } else {
                    baseDistance
                }

                // Tính pace dựa trên loại buổi tập
                val basePace = plan.input.averagePace ?: 6.0
                val pace = when (sessionType) {
                    ETrainingSessionType.LONG_RUN -> basePace + 1.0 // Chạy dài thì chậm hơn
                    ETrainingSessionType.SPEED_WORK -> (basePace - 1.0).coerceAtLeast(4.0) // Nhanh hơn
                    ETrainingSessionType.RECOVERY_RUN -> basePace + 1.5 // Chậm hơn nhiều
                    else -> 0.0
                }

                // Tạo session
                val session = TrainingSession(
                    name = when (sessionType) {
                        ETrainingSessionType.LONG_RUN -> "Chạy dài tuần $week"
                        ETrainingSessionType.SPEED_WORK -> "Tập tốc độ tuần $week"
                        ETrainingSessionType.RECOVERY_RUN -> "Chạy hồi phục tuần $week"
                        else -> "Nghỉ"
                    },
                    type = sessionType,
                    distance = distance,
                    pace = pace,
                    notes = "Buổi tập tự động tạo cho tuần $week",
                )

                val savedSession = trainingSessionRepository.save(session)

                // Tạo training day
                val trainingDay = TrainingDay().apply {
                    this.plan = plan
                    this.session = savedSession
                    this.week = week
                    this.dayOfWeek = day
                }

                trainingDays.add(trainingDay)
            }
        }

        return trainingDays
    }
}