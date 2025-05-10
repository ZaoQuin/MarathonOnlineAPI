package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.config.AITrainingProperties
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
    private val userRepository: UserRepository,
    private val aiTrainingProperties: AITrainingProperties
): AITrainingPlanService {
    val apiKey = aiTrainingProperties.api
    private val GROQ_MODEL = "llama3-70b-8192"

    override fun generateTrainingDays(input: TrainingPlanInput, plan: TrainingPlan): List<TrainingDay> {
        // Tạo prompt cho AI dựa trên thông tin đầu vào
        val prompt = createPromptForAI(input)

        // Gọi API AI để tạo lịch trình
        val aiResponse = callAIApi(prompt)

        // Xử lý phản hồi từ AI và tạo các ngày luyện tập, bao gồm các ngày nghỉ
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
        • Tổng số tuần luyện tập: 4
        • Số buổi tập mỗi tuần: ${input.daysPerWeek}

        ✳️ **YÊU CẦU ĐẦU RA**:
        Trả về một **mảng JSON hợp lệ**, KHÔNG thêm văn bản giải thích. Mỗi phần tử trong mảng tương ứng với một ngày tập luyện thực sự trong lịch trình (không bao gồm các ngày nghỉ), với các trường là bắt buộc, có định dạng:

        {
          "week": [số tuần, từ 1 đến 4],
          "dayOfWeek": [ngày trong tuần, từ 1 (Thứ Hai) đến 7 (Chủ Nhật)],
          "session": {
            "name": "[tên buổi tập]",
            "type": "[LONG_RUN | RECOVERY_RUN | SPEED_WORK]",
            "distance": [số km, số thập phân],
            "pace": [phút/km, số thập phân],
            "notes": "Nhắc nhở ngắn tùy thuộc loại hình tập luyện, ví dụ: 'Giữ pace ổn định toàn buổi', "
          }
        }

        📌 **Quan trọng**:
        • Chỉ tạo các mục cho các ngày tập luyện thực sự (${input.daysPerWeek} ngày mỗi tuần), không cần bao gồm các ngày nghỉ.
        • Hệ thống sẽ tự động xử lý các ngày nghỉ ngơi (REST).
        • Mỗi tuần nên có ${input.daysPerWeek} phần tử.

        🧠 **Nguyên tắc xây dựng kế hoạch**:
        1. LONG_RUN tăng dần qua từng tuần, bắt đầu từ ${(input.maxDistance ?: 10.0).coerceAtLeast(5.0)} km.
        2. SPEED_WORK cần đa dạng (fartlek, interval, tempo, hill repeats).
        3. RECOVERY_RUN nên chậm hơn pace trung bình 1–2 phút/km.
        4. Tránh xếp `"LONG_RUN"` và `"SPEED_WORK"` vào hai ngày liên tiếp.
        5. Phân bổ các buổi tập hợp lý trong tuần và tránh dồn tập vào các ngày liên tiếp.
        6. Sử dụng tiếng Việt.

        📊 **Số phần tử trong mảng đầu ra**: ${input.daysPerWeek!! * 4} phần tử (${input.daysPerWeek} ngày/tuần × 4 tuần).

        👉 Nhắc lại: **Chỉ trả về mảng JSON hợp lệ cho các ngày tập luyện thực sự. Không thêm văn bản mô tả nào khác**.
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
            val jsonResponse = JSONObject(responseBody)
            return jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    private fun parseAIResponse(aiResponse: String, plan: TrainingPlan): List<TrainingDay> {
        val trainingDays = mutableListOf<TrainingDay>()
        val trainingMap = mutableMapOf<Pair<Int, Int>, TrainingDay>() // Map of (week, dayOfWeek) to TrainingDay

        try {
            // Tìm và trích xuất phần JSON từ phản hồi
            val jsonPattern = Pattern.compile("\\[\\s*\\{.*\\}\\s*\\]", Pattern.DOTALL)
            val matcher = jsonPattern.matcher(aiResponse)

            if (matcher.find()) {
                val jsonArray = JSONArray(matcher.group())

                // Xử lý các ngày tập luyện do AI tạo ra
                for (i in 0 until jsonArray.length()) {
                    val dayJson = jsonArray.getJSONObject(i)
                    val sessionJson = dayJson.getJSONObject("session")
                    val week = dayJson.getInt("week")
                    val dayOfWeek = dayJson.getInt("dayOfWeek")

                    // Tạo session cho ngày tập luyện
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
                        this.week = week
                        this.dayOfWeek = dayOfWeek
                    }

                    trainingMap[Pair(week, dayOfWeek)] = trainingDay
                }

                // Tạo đầy đủ các ngày cho toàn bộ lịch trình (4 tuần x 7 ngày)
                for (week in 1..4) {
                    for (dayOfWeek in 1..7) {
                        val key = Pair(week, dayOfWeek)

                        // Nếu ngày này không phải là ngày tập được gen bởi AI, tạo ngày nghỉ
                        if (!trainingMap.containsKey(key)) {
                            // Tạo session nghỉ ngơi
                            val restSession = TrainingSession(
                                name = "Nghỉ ngơi",
                                type = ETrainingSessionType.REST,
                                distance = 0.0,
                                pace = 0.0,
                                notes = "Ngày nghỉ phục hồi"
                            )

                            val savedRestSession = trainingSessionRepository.save(restSession)

                            // Tạo training day nghỉ ngơi
                            val restDay = TrainingDay().apply {
                                this.plan = plan
                                this.session = savedRestSession
                                this.week = week
                                this.dayOfWeek = dayOfWeek
                            }

                            trainingMap[key] = restDay
                        }
                    }
                }

                // Chuyển tất cả các ngày từ map vào danh sách cuối cùng
                trainingDays.addAll(trainingMap.values)
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
        val weeks = 4
        val daysPerWeek = plan.input.daysPerWeek ?: 4
        val maxDistance = plan.input.maxDistance ?: 10.0
        val avgPace = plan.input.averagePace ?: 6.0

        // Chọn các ngày tập luyện tốt nhất trong tuần dựa trên số ngày tập
        val trainingDaysOfWeek = when (daysPerWeek) {
            3 -> listOf(2, 4, 7) // Thứ 3, 5, CN
            4 -> listOf(2, 4, 6, 7) // Thứ 3, 5, 7, CN
            5 -> listOf(2, 3, 5, 6, 7) // Thứ 3, 4, 6, 7, CN
            6 -> listOf(1, 2, 4, 5, 6, 7) // Thứ 2, 3, 5, 6, 7, CN
            7 -> (1..7).toList() // Cả tuần
            else -> listOf(2, 4, 7) // Mặc định 3 ngày: Thứ 3, 5, CN
        }

        for (week in 1..weeks) {
            for (day in 1..7) {
                // Xác định session type dựa vào ngày trong tuần
                val sessionType = if (day in trainingDaysOfWeek) {
                    when {
                        day == 7 -> ETrainingSessionType.LONG_RUN // Chủ nhật luôn là ngày chạy dài
                        day == 2 || day == 5 -> ETrainingSessionType.SPEED_WORK // Thứ 3, 6 tập tốc độ
                        else -> ETrainingSessionType.RECOVERY_RUN // Các ngày còn lại là hồi phục
                    }
                } else {
                    ETrainingSessionType.REST // Ngày không trong lịch tập là ngày nghỉ
                }

                // Điều chỉnh thông số buổi tập dựa vào loại session và tuần luyện tập
                val distance = when (sessionType) {
                    ETrainingSessionType.LONG_RUN -> {
                        val baseDistance = maxDistance.coerceAtLeast(5.0)
                        when (week) {
                            1 -> baseDistance
                            2 -> baseDistance * 1.2
                            3 -> baseDistance * 1.5
                            4 -> if (plan.input.goal == ETrainingPlanInputGoal.MARATHON_FINISH ||
                                plan.input.goal == ETrainingPlanInputGoal.MARATHON_TIME) 42.195 else baseDistance * 1.7
                            else -> baseDistance
                        }
                    }
                    ETrainingSessionType.SPEED_WORK -> 5.0 + (week * 0.5).coerceAtMost(2.0)
                    ETrainingSessionType.RECOVERY_RUN -> 3.0 + (week * 0.5).coerceAtMost(2.0)
                    else -> 0.0
                }

                val pace = when (sessionType) {
                    ETrainingSessionType.LONG_RUN -> avgPace + 0.5 // Chạy dài thì chậm hơn một chút
                    ETrainingSessionType.SPEED_WORK -> (avgPace - 0.5).coerceAtLeast(4.0) // Tốc độ nhanh hơn
                    ETrainingSessionType.RECOVERY_RUN -> avgPace + 1.5 // Chạy hồi phục chậm hơn nhiều
                    else -> 0.0
                }

                // Tạo session name và notes phong phú hơn
                val (sessionName, sessionNotes) = when (sessionType) {
                    ETrainingSessionType.LONG_RUN -> {
                        val name = "Chạy dài tuần $week"
                        val notes = "Giữ nhịp đều, tập trung vào sức bền và cảm giác thoải mái"
                        Pair(name, notes)
                    }
                    ETrainingSessionType.SPEED_WORK -> {
                        val speedWorkTypes = listOf(
                            "Interval" to "Chạy nhanh 400m, nghỉ 200m, lặp lại 6-8 lần",
                            "Tempo" to "Duy trì pace nhanh trong 15-20 phút liên tục",
                            "Fartlek" to "Xen kẽ tốc độ nhanh-chậm tùy cảm giác",
                            "Hill Repeats" to "Tìm dốc 100-200m, chạy lên dốc rồi đi bộ xuống"
                        )
                        val (type, note) = speedWorkTypes[(week - 1) % speedWorkTypes.size]
                        Pair("$type tuần $week", note)
                    }
                    ETrainingSessionType.RECOVERY_RUN -> {
                        val name = "Chạy hồi phục tuần $week"
                        val notes = "Chạy chậm thoải mái, tập trung phục hồi cơ bắp"
                        Pair(name, notes)
                    }
                    else -> Pair("Nghỉ ngơi", "Thư giãn và phục hồi, có thể tập nhẹ các động tác kéo giãn")
                }

                // Tạo session
                val session = TrainingSession(
                    name = sessionName,
                    type = sessionType,
                    distance = distance.round(2),
                    pace = pace.round(2),
                    notes = sessionNotes
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

    // Extension function để làm tròn số thập phân
    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}