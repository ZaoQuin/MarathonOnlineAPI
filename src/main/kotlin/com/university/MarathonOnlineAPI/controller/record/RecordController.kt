package com.university.MarathonOnlineAPI.controller.record

import com.university.MarathonOnlineAPI.controller.StringResponse
import com.university.MarathonOnlineAPI.dto.CreateRecordRequest
import com.university.MarathonOnlineAPI.dto.RecordApprovalDTO
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.entity.ERecordApprovalStatus
import com.university.MarathonOnlineAPI.exception.RecordException
import com.university.MarathonOnlineAPI.service.RecordApprovalService
import com.university.MarathonOnlineAPI.service.RecordService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/record")
class RecordController(
    private val recordService: RecordService,
    private val recordApprovalService: RecordApprovalService
) {

    private val logger = LoggerFactory.getLogger(RecordController::class.java)

    @PostMapping
    fun addRaceAndSaveIntoRegistration(
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid newRecord: CreateRecordRequest
    ): ResponseEntity<Any> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val addedRecord = recordService.addRecord(newRecord, jwt)
            logger.error("Show addedRace: $addedRecord")
            ResponseEntity(addedRecord, HttpStatus.CREATED)
        } catch (e: RecordException) {
            logger.error("Error adding record: ${e.message}")
            ResponseEntity("Race error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteRace(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            recordService.deleteRecordById(id)
            logger.info("Race with ID $id deleted successfully")
            ResponseEntity.ok("Race with ID $id deleted successfully")
        } catch (e: RecordException) {
            logger.error("Failed to delete record with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to delete record with ID $id: ${e.message}")
        } catch (e: Exception) {
            logger.error("Failed to delete record with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete record with ID $id: ${e.message}")
        }
    }

    @PutMapping
    fun updateRace(@RequestBody @Valid recordDTO: RecordDTO): ResponseEntity<RecordDTO> {
        return try {
            val updatedRace = recordService.updateRecord(recordDTO)
            ResponseEntity(updatedRace, HttpStatus.OK)
        } catch (e: RecordException) {
            logger.error("Race exception: ${e.message}")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database access error: ${e.message}")
            throw RecordException("Database error occurred: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error updating record: ${e.message}")
            throw RecordException("Error updating record: ${e.message}")
        }
    }

    @GetMapping
    fun getRaces(): ResponseEntity<List<RecordDTO>> {
        return try {
            val records = recordService.getRecords()
            ResponseEntity(records, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getRaces: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/runner")
    fun getByRunnerToken(@RequestHeader("Authorization") token: String): ResponseEntity<List<RecordDTO>> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val records = recordService.getRecordsByToken(jwt)
            ResponseEntity(records, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error("Error in getRaces: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    fun getRaceById(@PathVariable id: Long): ResponseEntity<RecordDTO> {
        return try {
            val foundRace = recordService.getById(id)
            ResponseEntity.ok(foundRace)
        } catch (e: RecordException) {
            logger.error("Error getting record by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting record by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/validate")
    fun validateRecord(@RequestBody recordDTO: RecordDTO): ResponseEntity<RecordApprovalDTO> {
        try {
            val approvalResult = recordApprovalService.analyzeRecordApproval(recordDTO)
            return ResponseEntity.ok(approvalResult)
        } catch (e: Exception) {
            logger.error("Lỗi khi xác thực bản ghi", e)
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    RecordApprovalDTO(
                        approvalStatus = ERecordApprovalStatus.PENDING,
                        fraudRisk = 50.0,
                        fraudType = "Lỗi hệ thống",
                        reviewNote = "Lỗi khi xử lý yêu cầu: ${e.message}"
                    )
                )
        }
    }

    /**
     * Endpoint để cập nhật trạng thái phê duyệt cho bản ghi đang ở trạng thái PENDING
     */
    @PutMapping("/{recordId}/approval")
    fun updateApprovalStatus(
        @PathVariable recordId: Long,
        @RequestBody approvalDTO: RecordApprovalDTO
    ): ResponseEntity<RecordApprovalDTO> {
        try {
            // Tìm bản ghi và cập nhật trạng thái phê duyệt
            val updatedApproval = recordApprovalService.updateApprovalStatus(recordId, approvalDTO)
            return ResponseEntity.ok(updatedApproval)
        } catch (e: Exception) {
            logger.error("Lỗi khi cập nhật trạng thái phê duyệt", e)
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    RecordApprovalDTO(
                        approvalStatus = ERecordApprovalStatus.PENDING,
                        fraudRisk = null,
                        fraudType = null,
                        reviewNote = "Lỗi khi cập nhật trạng thái phê duyệt: ${e.message}"
                    )
                )
        }
    }

    @GetMapping("/user/{userId}/history")
    fun getUserHistory(
        @PathVariable userId: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime?
    ): ResponseEntity<List<RecordDTO>> {
        return try {
            val records = recordService.getRecordsByUserId(userId, startDate, endDate)
            logger.info("Đã lấy ${records.size} bản ghi lịch sử cho userId $userId từ ${startDate ?: "không giới hạn"} đến ${endDate ?: "không giới hạn"}")
            ResponseEntity.ok(records)
        } catch (e: Exception) {
            logger.error("Lỗi khi lấy lịch sử bản ghi cho userId $userId: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }

    @PostMapping("/sync")
    fun syncRecords(
        @RequestHeader("Authorization") token: String,
        @RequestBody recordDTOs: List<CreateRecordRequest>
    ): ResponseEntity<StringResponse> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val savedRecords = recordService.sync(recordDTOs, jwt)
            logger.info("Đồng bộ thành công" + savedRecords)
            ResponseEntity.ok(StringResponse("Đồng bộ thành công"))
        } catch (e: Exception) {
            logger.error("Lỗi khi đồng bộ dữ liệu: ${e.message}")
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StringResponse(e.message!!))
        }
    }
}
