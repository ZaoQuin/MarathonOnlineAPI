package com.university.MarathonOnlineAPI.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.university.MarathonOnlineAPI.dto.RecordApprovalDTO
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.entity.ERecordApprovalStatus
import com.university.MarathonOnlineAPI.mapper.RecordApprovalMapper
import com.university.MarathonOnlineAPI.mapper.RecordMapper
import com.university.MarathonOnlineAPI.repos.RecordApprovalRepository
import com.university.MarathonOnlineAPI.repos.RecordRepository
import com.university.MarathonOnlineAPI.service.RecordApprovalService
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

@Component
class RecordApprovalServiceImpl(
    private val recordRepository: RecordRepository,
    private val recordMapper: RecordMapper,
    private val recordApprovalRepository: RecordApprovalRepository,
    private val recordApprovalMapper: RecordApprovalMapper,
    private val objectMapper: ObjectMapper
): RecordApprovalService {

    override fun analyzeRecordApproval(recordDTO: RecordDTO): RecordApprovalDTO {
        if (recordDTO.steps == null || recordDTO.distance == null || recordDTO.timeTaken == null) {
            return RecordApprovalDTO(
                approvalStatus = ERecordApprovalStatus.REJECTED,
                fraudRisk = 100.0,
                fraudType = "Dữ liệu thiếu",
                reviewNote = "Thiếu thông tin cần thiết: số bước, khoảng cách hoặc thời gian."
            )
        }

        val recordApproval = analyzeRecord(recordDTO)
        val savedRecordApproval = recordApprovalRepository.save(recordApprovalMapper.toEntity(recordApproval))

        return recordApprovalMapper.toDto(savedRecordApproval)
    }

    override fun saveRecordApproval(recordDTO: RecordDTO): RecordApprovalDTO {
        TODO("Not yet implemented")
    }

    override fun updateApprovalStatus(recordId: Long, approvalDTO: RecordApprovalDTO): RecordApprovalDTO {
        TODO("Not yet implemented")
    }

    override fun getRecordApproval(recordId: Long): RecordApprovalDTO? {
        TODO("Not yet implemented")
    }

    override fun getPendingRecords(): List<RecordDTO> {
        TODO("Not yet implemented")
    }

    private fun analyzeRecord(recordDTO: RecordDTO): RecordApprovalDTO {
        try {
            val recordJson = objectMapper.writeValueAsString(recordDTO)

            val pythonPath = System.getenv("PYTHON_PATH") ?: "python"
            val scriptDir = File("pythonScripts")
            val scriptPath = File(scriptDir, "record_validator.py").absolutePath

            if (!scriptDir.exists()) {
                scriptDir.mkdirs()
            }

            // Tạo một temporary file để chứa JSON thay vì truyền qua command line
            val tempFile = File.createTempFile("record_", ".json", scriptDir)
            tempFile.deleteOnExit() // Đảm bảo file sẽ bị xóa khi JVM kết thúc
            tempFile.writeText(recordJson)

            // Truyền đường dẫn đến file JSON thay vì nội dung JSON
            val processBuilder = ProcessBuilder(pythonPath, scriptPath, tempFile.absolutePath, "--file")
            processBuilder.directory(scriptDir)
            processBuilder.redirectErrorStream(true)

            val process = processBuilder.start()

            val reader = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
            val output = reader.lines().collect(Collectors.joining("\n"))

            val exitCode = process.waitFor()
            debugPythonExecution(output, exitCode)

            // Xóa file tạm sau khi xử lý xong
            tempFile.delete()

            if (exitCode != 0) {
                return RecordApprovalDTO(
                    approvalStatus = ERecordApprovalStatus.PENDING,
                    fraudRisk = 50.0,
                    fraudType = "Lỗi phân tích",
                    reviewNote = "Lỗi khi chạy script phân tích"
                )
            }

            val jsonResult = extractJsonFromOutput(output)

            return parseValidationResult(jsonResult)

        } catch (e: Exception) {
            e.printStackTrace() // In ra stack trace để debug chi tiết hơn
            return RecordApprovalDTO(
                approvalStatus = ERecordApprovalStatus.PENDING,
                fraudRisk = 50.0,
                fraudType = "Lỗi hệ thống",
                reviewNote = "Lỗi khi phân tích bản ghi: ${e.message}"
            )
        }
    }
    /**
     * Tìm chuỗi JSON trong output của Python script
     */
    private fun extractJsonFromOutput(output: String): String {
        val beginMarker = "--- BEGIN JSON RESULT ---"
        val endMarker = "--- END JSON RESULT ---"

        val beginIndex = output.indexOf(beginMarker)
        val endIndex = output.indexOf(endMarker)

        if (beginIndex != -1 && endIndex != -1 && beginIndex < endIndex) {
            val json = output.substring(beginIndex + beginMarker.length, endIndex).trim()
            return json
        }

        val jsonStartIndex = output.lastIndexOf('{')
        if (jsonStartIndex != -1) {
            val json = output.substring(jsonStartIndex)
            return json
        }

        return """{"approvalStatus":"PENDING","fraudRisk":50.0,"fraudType":"Lỗi phân tích","reviewNote":"Không thể phân tích kết quả"}"""
    }

    /**
     * Phân tích JSON thành đối tượng RecordApprovalDTO
     */
    private fun parseValidationResult(jsonOutput: String): RecordApprovalDTO {
        return try {
            val resultMap = objectMapper.readValue(jsonOutput, Map::class.java)
            val approvalStatus = when (resultMap["approvalStatus"] as String?) {
                "APPROVED" -> ERecordApprovalStatus.APPROVED
                "REJECTED" -> ERecordApprovalStatus.REJECTED
                else -> ERecordApprovalStatus.PENDING
            }

            val result = RecordApprovalDTO(
                approvalStatus = approvalStatus,
                fraudRisk = resultMap["fraudRisk"] as Double?,
                fraudType = resultMap["fraudType"] as String?,
                reviewNote = resultMap["reviewNote"] as String?
            )
            return result
        } catch (e: Exception) {
            RecordApprovalDTO(
                approvalStatus = ERecordApprovalStatus.PENDING,
                fraudRisk = 50.0,
                fraudType = "Lỗi phân tích",
                reviewNote = "Không thể phân tích kết quả: ${e.message}"
            )
        }
    }

    // Thêm phương thức này vào lớp RecordApprovalServiceImpl của bạn
    private fun debugPythonExecution(output: String, exitCode: Int) {
        println("Python execution completed with exit code: $exitCode")
        println("Output:")
        println(output)
    }
}