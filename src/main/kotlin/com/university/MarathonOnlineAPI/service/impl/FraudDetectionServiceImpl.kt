package com.university.MarathonOnlineAPI.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.university.MarathonOnlineAPI.controller.fraundDetection.FraudAnalysisRequest
import com.university.MarathonOnlineAPI.controller.fraundDetection.FraudAnalysisResponse
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.service.FraudDetectionService
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors


@Service
class FraudDetectionServiceImpl(private val objectMapper: ObjectMapper): FraudDetectionService {

    @Throws(IOException::class)
    override fun analyzeMarathonData(request: FraudAnalysisRequest?): FraudAnalysisResponse {
        if (request?.marathonData.isNullOrEmpty()) {
            throw IllegalArgumentException("marathonData must not be null or empty")
        }
        // Tạo file CSV tạm thời từ dữ liệu đầu vào
        val tempFile: Path = createTempCsvFile(request!!.marathonData!!)
        return try {
            // Xác định đường dẫn tới Python và thư mục chứa script
            val pythonPath = System.getenv("PYTHON_PATH") ?: "python"  // Fallback to "python" if not set
            val scriptDir = File("pythonScripts") // thư mục chứa script Python
            val scriptPath = File(scriptDir, "fraud_analysis.py").absolutePath

            // Kiểm tra và đảm bảo thư mục script tồn tại
            if (!scriptDir.exists()) {
                scriptDir.mkdirs()
            }

            // Log thông tin để debug
            println("Running Python script: $pythonPath $scriptPath ${tempFile.toAbsolutePath()}")

            // Chạy script Python để phân tích
            val processBuilder = ProcessBuilder(
                pythonPath,
                scriptPath,
                tempFile.toAbsolutePath().toString()
            )

            // Đặt thư mục làm việc hiện tại
            processBuilder.directory(scriptDir)

            // Chuyển hướng error stream để có thể đọc
            processBuilder.redirectErrorStream(true)

            // Bắt đầu quá trình
            val process = processBuilder.start()

            // Đọc output từ quá trình với encoding UTF-8
            val reader = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
            val output = reader.lines().collect(Collectors.joining("\n"))

            // Đợi quá trình hoàn thành
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                println("Python script exited with code $exitCode")
                println("Output: $output")
                // Trả về một response mặc định thay vì throw exception
                return FraudAnalysisResponse(
                    totalRecords = request.marathonData?.size ?: 0,
                    totalFraudRecords = 0,
                    fraudUserIds = emptyList(),
                    userRiskScores = emptyMap(),
                    fraudRecordDetails = emptyList()
                )
            }

            // Phân tích JSON từ output
            val jsonResult = extractJsonFromOutput(output)
            println("Extracted JSON: $jsonResult")

            // Parse kết quả JSON thành đối tượng response
            val response = parseJsonResults(jsonResult)

            // Kiểm tra kết quả và xử lý trường hợp null
            handleNullValues(response, request.marathonData?.size ?: 0)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            println("Process interrupted: ${e.message}")
            // Trả về response mặc định khi bị interrupt
            FraudAnalysisResponse(
                totalRecords = request.marathonData?.size ?: 0,
                totalFraudRecords = 0,
                fraudUserIds = emptyList(),
                userRiskScores = emptyMap(),
                fraudRecordDetails = emptyList()
            )
        } catch (e: Exception) {
            println("Unexpected error: ${e.message}")
            e.printStackTrace()
            // Trả về response mặc định khi có lỗi
            FraudAnalysisResponse(
                totalRecords = request.marathonData?.size ?: 0,
                totalFraudRecords = 0,
                fraudUserIds = emptyList(),
                userRiskScores = emptyMap(),
                fraudRecordDetails = emptyList()
            )
        } finally {
            // Xóa file tạm thời
            Files.deleteIfExists(tempFile)
        }
    }

    /**
     * Xử lý các giá trị null trong response
     */
    private fun handleNullValues(response: FraudAnalysisResponse, totalRecords: Int): FraudAnalysisResponse {
        // Đảm bảo không có giá trị null trong response
        if (response.totalRecords == null) {
            response.totalRecords = totalRecords
        }
        if (response.totalFraudRecords == null) {
            response.totalFraudRecords = 0
        }
        if (response.fraudUserIds == null) {
            response.fraudUserIds = emptyList()
        }
        if (response.userRiskScores == null) {
            response.userRiskScores = emptyMap()
        }
        if (response.fraudRecordDetails == null) {
            response.fraudRecordDetails = emptyList()
        }
        return response
    }

    @Throws(IOException::class)
    private fun createTempCsvFile(records: List<RecordDTO>): Path {
        // Tạo file tạm thời
        val tempFile: Path = Files.createTempFile("marathon_data_", ".csv")

        // Tạo nội dung CSV
        val lines: MutableList<String> = ArrayList()

        // Header
        lines.add("Id,UserId,TotalSteps,TotalDistance,TimeTaken,AvgSpeed,Timestamp")

        // Dữ liệu
        for (record in records) {
            lines.add(
                String.format(
                    "%s,%s,%d,%.2f,%d,%.2f,%s",
                    record.id,
                    record.user?.id ?: "",  // Sử dụng chuỗi rỗng nếu user là null
                    record.steps,
                    record.distance,
                    record.timeTaken,
                    record.avgSpeed,
                    record.timestamp
                )
            )
        }

        // Ghi vào file với UTF-8 encoding
        Files.write(tempFile, lines, StandardCharsets.UTF_8)
        return tempFile
    }

    /**
     * Tìm chuỗi JSON trong output của Python script
     */
    private fun extractJsonFromOutput(output: String): String {
        // Tìm marker bắt đầu và kết thúc JSON
        val beginMarker = "--- BEGIN JSON RESULT ---"
        val endMarker = "--- END JSON RESULT ---"

        val beginIndex = output.indexOf(beginMarker)
        val endIndex = output.indexOf(endMarker)

        if (beginIndex != -1 && endIndex != -1 && beginIndex < endIndex) {
            // Trích xuất JSON từ giữa các marker
            return output.substring(beginIndex + beginMarker.length, endIndex).trim()
        }

        // Fallback: tìm JSON object cuối cùng trong output
        val jsonStartIndex = output.lastIndexOf('{')
        if (jsonStartIndex != -1) {
            return output.substring(jsonStartIndex)
        }

        // Nếu không tìm thấy JSON, trả về JSON object rỗng
        return """{"totalRecords":0,"totalFraudRecords":0,"fraudUserIds":[],"userRiskScores":{},"fraudRecordDetails":[]}"""
    }

    /**
     * Phân tích JSON thành đối tượng FraudAnalysisResponse
     */
    @Throws(IOException::class)
    private fun parseJsonResults(jsonOutput: String): FraudAnalysisResponse {
        try {
            // Sử dụng Jackson để chuyển đổi JSON thành đối tượng FraudAnalysisResponse trực tiếp
            return objectMapper.readValue(jsonOutput, FraudAnalysisResponse::class.java)
        } catch (e: Exception) {
            println("JSON parsing error. Input JSON: $jsonOutput")
            println("Error: ${e.message}")
            // Trả về đối tượng mặc định thay vì throw exception
            return FraudAnalysisResponse(
                totalRecords = 0,
                totalFraudRecords = 0,
                fraudUserIds = emptyList(),
                userRiskScores = emptyMap(),
                fraudRecordDetails = emptyList()
            )
        }
    }
}