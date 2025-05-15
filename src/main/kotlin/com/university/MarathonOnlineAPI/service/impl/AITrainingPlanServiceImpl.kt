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
import java.util.regex.Pattern

@Service
class AITrainingPlanServiceImpl(
    private val trainingSessionRepository: TrainingSessionRepository,
    private val trainingDayRepository: TrainingDayRepository,
    private val aiTrainingProperties: AITrainingProperties
) : AITrainingPlanService {
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
    Tạo lịch trình huấn luyện marathon 4 tuần được cá nhân hóa dựa trên thông tin sau:

    THÔNG TIN RUNNER:
    • Trình độ: ${input.level}
    • Mục tiêu: ${input.goal}
    • Khoảng cách dài nhất từng chạy: ${input.maxDistance ?: 10.0} km
    • Tốc độ trung bình: ${input.averagePace ?: 6.0} phút/km
    • Số buổi tập mỗi tuần: ${input.daysPerWeek ?: 4}

    NGUYÊN TẮC HUẤN LUYỆN:
    • Mỗi tuần phải có chính xác ${input.daysPerWeek} ngày tập, còn lại là ngày nghỉ
    • LONG_RUN: Mỗi tuần có 1 buổi, thường vào cuối tuần, tăng dần qua từng tuần
    • SPEED_WORK: Mỗi tuần có 1-2 buổi, không đặt ngày liên tiếp với LONG_RUN
    • RECOVERY_RUN: Điền vào các ngày tập còn lại, pace chậm hơn pace trung bình 1-2 phút/km
    • Phân bố ngày tập đều trong tuần, tránh tập dồn nhiều ngày liên tiếp

    TIÊU CHUẨN KẾ HOẠCH:
    • Tuần 1: Thiết lập nền tảng, khối lượng vừa phải
    • Tuần 2: Tăng khối lượng 10-15%
    • Tuần 3: Tuần nặng nhất, tăng khối lượng thêm 15-20%
    • Tuần 4: Giảm khối lượng (taper), chuẩn bị cho sự kiện

    YÊU CẦU ĐẦU RA:
    Trả về CHÍNH XÁC một mảng JSON có ${input.daysPerWeek!! * 4} phần tử (${input.daysPerWeek} ngày/tuần × 4 tuần).
    
    Mỗi phần tử phải có cấu trúc sau:
    {
      "week": [số tuần, từ 1-4],
      "dayOfWeek": [ngày trong tuần, 1=Thứ Hai đến 7=Chủ Nhật],
      "session": {
        "name": "[tên buổi tập cụ thể, VD: 'Tempo Run Tuần 2' hoặc 'Long Run 18km']",
        "type": "[LONG_RUN | RECOVERY_RUN | SPEED_WORK]",
        "distance": [số km, làm tròn 1 chữ số thập phân],
        "pace": [phút/km, làm tròn 1 chữ số thập phân],
        "notes": "[hướng dẫn chi tiết cho buổi tập]"
      }
    }

    LOẠI HÌNH TẬP LUYỆN ĐA DẠNG:
    • LONG_RUN: Tốc độ nhẹ nhàng, tăng dần khoảng cách qua các tuần
    • SPEED_WORK đa dạng:
        - Interval: Xen kẽ tốc độ cao và nghỉ ngơi (VD: 8x400m tốc độ cao, nghỉ 200m)
        - Tempo: Duy trì tốc độ race pace trong thời gian dài (15-30 phút)
        - Fartlek: Chạy tự do thay đổi tốc độ theo cảm giác
        - Hill Repeats: Lặp lại chạy lên dốc để tăng sức mạnh

    ĐIỀU CHỈNH THEO TRÌNH ĐỘ:
    ${
            when (input.level) {
                ETrainingPlanInputLevel.BEGINNER -> "• Beginner: Ưu tiên xây dựng sức bền cơ bản, nhiều RECOVERY_RUN, LONG_RUN không quá 50% tổng quãng đường tuần"
                ETrainingPlanInputLevel.INTERMEDIATE  -> "• Intermediate: Cân bằng SPEED_WORK và RECOVERY_RUN, LONG_RUN đạt 30-40% tổng quãng đường tuần"
                else -> "• Advanced: Tăng cường SPEED_WORK đa dạng, LONG_RUN có thể đạt 25-30% tổng quãng đường tuần"
            }
        }

    ĐIỀU CHỈNH THEO MỤC TIÊU:
    ${
            when (input.goal) {
                ETrainingPlanInputGoal.MARATHON_FINISH -> "• Hoàn thành Marathon: Tập trung vào LONG_RUN và sức bền tổng thể, tuần 3 đạt ít nhất 30km"
                ETrainingPlanInputGoal.MARATHON_TIME -> "• Cải thiện thời gian Marathon: Tăng cường SPEED_WORK với Tempo và Marathon-pace runs"
                ETrainingPlanInputGoal.HALF_MARATHON_FINISH -> "• Hoàn thành Half Marathon: LONG_RUN tuần 3 đạt 15-18km"
                ETrainingPlanInputGoal.HALF_MARATHON_TIME -> "• Cải thiện Half Marathon: Kết hợp Tempo runs ở race pace và interval training"
                ETrainingPlanInputGoal.TEN_KM_FINISH -> "• Hoàn thành 10K: LONG_RUN tuần 3 đạt 8-12km"
                ETrainingPlanInputGoal.TEN_KM_TIME -> "• Cải thiện 10K: Nhiều interval training với tốc độ 10K pace"
                ETrainingPlanInputGoal.FIVE_KM_FINISH -> "• Hoàn thành 5K: LONG_RUN không quá 8km"
                ETrainingPlanInputGoal.FIVE_KM_TIME -> "• Cải thiện 5K: Short intervals với tốc độ 5K race pace"
                else -> "• Mục tiêu chung: Cân bằng giữa sức bền và tốc độ"
            }
        }

    QUAN TRỌNG:
    • CHỈ trả về mảng JSON, KHÔNG thêm bất kỳ văn bản giải thích nào khác
    • Sử dụng tiếng Việt cho tất cả nội dung
    • Đảm bảo đủ CHÍNH XÁC ${input.daysPerWeek!! * 4} phần tử trong mảng, mỗi phần tử là một ngày tập thực sự
    • Đảm bảo JSON hợp lệ về cú pháp, không có lỗi dấu phẩy hoặc ngoặc
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
                                plan.input.goal == ETrainingPlanInputGoal.MARATHON_TIME
                            ) 42.195 else baseDistance * 1.7

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