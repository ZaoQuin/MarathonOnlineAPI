package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.dto.CreateRecordRequest
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.dto.RunningStatsDTO
import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.*
import com.university.MarathonOnlineAPI.exception.AuthenticationException
import com.university.MarathonOnlineAPI.exception.RecordException
import com.university.MarathonOnlineAPI.handler.RecordMergerHandler
import com.university.MarathonOnlineAPI.mapper.RecordMapper
import com.university.MarathonOnlineAPI.mapper.UserMapper
import com.university.MarathonOnlineAPI.repos.RecordApprovalRepository
import com.university.MarathonOnlineAPI.repos.RecordRepository
import com.university.MarathonOnlineAPI.repos.RegistrationRepository
import com.university.MarathonOnlineAPI.repos.TrainingDayRepository
import com.university.MarathonOnlineAPI.service.RecordApprovalService
import com.university.MarathonOnlineAPI.service.RecordService
import com.university.MarathonOnlineAPI.service.TokenService
import com.university.MarathonOnlineAPI.service.UserService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class RecordServiceImpl @Autowired constructor(
    private val recordRepository: RecordRepository,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val recordMapper: RecordMapper,
    private val userMapper: UserMapper,
    private val recordApprovalService: RecordApprovalService,
    private val recordApprovalRepository: RecordApprovalRepository,
    private val recordMergerHandler: RecordMergerHandler,
    private val registrationRepository: RegistrationRepository,
    private val trainingDayRepository: TrainingDayRepository
) : RecordService {

    private val logger = LoggerFactory.getLogger(RecordServiceImpl::class.java)

    override fun addRecord(newRecord: CreateRecordRequest, jwt: String): RecordDTO {
        logger.info("Received RecordDTO: $newRecord")
        try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")

            val recordDTO = RecordDTO(
                steps = newRecord.steps,
                distance = newRecord.distance,
                avgSpeed = newRecord.avgSpeed,
                heartRate = newRecord.heartRate?: null,
                user = userDTO,
                startTime = newRecord.startTime,
                endTime = newRecord.endTime,
                timeTaken = java.time.Duration.between(newRecord.startTime, newRecord.endTime).seconds,
                source = newRecord.source
            )

            var record = recordRepository.save(recordMapper.toEntity(recordDTO))
            val savedRecordApproval = recordApprovalService.analyzeRecordApproval(
                recordMapper.toDto(record)
            )
            val approvalEntity = recordApprovalRepository.findById(savedRecordApproval.id!!)
                .orElseThrow { RuntimeException("Approval not found") }

            record.approval = approvalEntity;

            return recordMapper.toDto(recordRepository.save(record))
        } catch (e: DataAccessException) {
            logger.error("Error saving race: ${e.message}")
            throw RecordException("Database error occurred while saving race: ${e.message}")
        }
    }

    override fun deleteRecordById(id: Long) {
        try {
            if (recordRepository.existsById(id)) {
                recordRepository.deleteById(id)
                logger.info("Record with ID $id deleted successfully")
            } else {
                throw RecordException("Record with ID $id not found")
            }
        } catch (e: DataAccessException) {
            logger.error("Error deleting race with ID $id: ${e.message}")
            throw RecordException("Database error occurred while deleting race: ${e.message}")
        }
    }

    override fun updateRecord(recordDTO: RecordDTO): RecordDTO {
        logger.info("Updating RecordDTO: $recordDTO")
        return try {
            val race = recordMapper.toEntity(recordDTO)
            recordRepository.save(race)
            recordMapper.toDto(race)
        } catch (e: DataAccessException) {
            logger.error("Error updating race: ${e.message}")
            throw RecordException("Database error occurred while updating race: ${e.message}")
        }
    }

    override fun getRecords(): List<RecordDTO> {
        return try {
            val races = recordRepository.findAll()
            val raceDTOs = races.map { race ->
                recordMapper.toDto(race)
            }
            raceDTOs
        } catch (e: DataAccessException) {
            logger.error("Error fetching races: ${e.message}")
            throw RecordException("Database error occurred while fetching races: ${e.message}")
        }
    }

    override fun getById(id: Long): RecordDTO {
        return try {
            val record = recordRepository.findById(id)
                .orElseThrow { RecordException("Record with ID $id not found") }

            recordMapper.toDto(record)
        } catch (e: DataAccessException) {
            logger.error("Error fetching race with ID $id: ${e.message}")
            throw RecordException("Database error occurred while fetching race: ${e.message}")
        }
    }

    override fun getRecordsByToken(jwt: String): List<RecordDTO> {
        try {
            val userDTO =
                tokenService.extractEmail(jwt)?.let { email ->
                    userService.findByEmail(email)
                } ?: throw AuthenticationException("Email not found in the token")

            val races = userDTO.id?.let { recordRepository.findByUserIdAndApprovalApprovalStatusIn(
                userDTO.id!!,
                listOf(ERecordApprovalStatus.PENDING, ERecordApprovalStatus.APPROVED)
            ) }
            return races?.map { recordMapper.toDto(it) }!!
        } catch (e: DataAccessException) {
            logger.error("Error saving race: ${e.message}")
            throw RecordException("Database error occurred while saving race: ${e.message}")
        }
    }

    override fun getRunningStatsByUser(userId: Long): RunningStatsDTO? {
        val records = recordRepository.findByUserIdAndApprovalApprovalStatusInOrderByStartTimeDesc(
            userId,
            listOf(ERecordApprovalStatus.PENDING, ERecordApprovalStatus.APPROVED)
        )

        if (records.isEmpty()) return null

        val recordDTOs = records.map { recordMapper.toDto(it) }

        val maxDistance = recordDTOs.maxOf { it.distance ?: 0.0 }

        val totalDistance = recordDTOs.sumOf { it.distance ?: 0.0 }
        val totalTimeInMinutes = recordDTOs.sumOf { (it.timeTaken ?: 0L) } / 60.0

        val averagePace = if (totalDistance > 0) totalTimeInMinutes / totalDistance else 0.0

        return RunningStatsDTO(
            maxDistance = "%.2f".format(maxDistance).toDouble(),
            averagePace = "%.2f".format(averagePace).toDouble()
        )
    }

    override fun getRecordsByUserId(userId: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<RecordDTO> {
        return recordRepository.findByUserIdAndApprovalApprovalStatusInAndStartTimeBetweenOrderByStartTimeDesc(
            userId,
            listOf(ERecordApprovalStatus.PENDING, ERecordApprovalStatus.APPROVED),
            startDate ?: LocalDateTime.MIN,
            endDate ?: LocalDateTime.now()
        ).map { recordMapper.toDto(it) }
    }

    override fun sync(recordDTOs: List<CreateRecordRequest>, jwt: String): List<RecordDTO> {
        return try {
            val userDTO = tokenService.extractEmail(jwt)?.let { email ->
                userService.findByEmail(email)
            } ?: throw AuthenticationException("Email not found in the token")

            logger.info("Starting sync for user ${userDTO.id} with ${recordDTOs.size} records")

            val mergedRecords = recordMergerHandler.mergeRecords(recordDTOs, userDTO)
            logger.info("After merge: ${mergedRecords.size} records to sync")

            val syncedRecords = mutableListOf<RecordDTO>()

            mergedRecords.forEach { recordDTO ->
                try {
                    val syncedRecord = syncOneRecord(recordDTO, userDTO)
                    syncedRecords.add(syncedRecord)
                    logger.info("Successfully synced record: ${syncedRecord.id}")
                } catch (e: Exception) {
                    logger.error("Error syncing record: ${e.message}", e)
                }
            }

            logger.info("Sync completed: ${syncedRecords.size}/${mergedRecords.size} records synced successfully")
            syncedRecords
        } catch (e: AuthenticationException) {
            logger.error("Authentication failed: ${e.message}", e)
            emptyList()
        } catch (e: Exception) {
            logger.error("Unexpected error during sync: ${e.message}", e)
            emptyList()
        }
    }

    @Transactional
    fun syncOneRecord(recordDTO: RecordDTO, userDTO: UserDTO): RecordDTO {
        logger.info("Syncing record: startTime=${recordDTO.startTime}, endTime=${recordDTO.endTime}")

        val exactMatch = findExactMatchRecord(recordDTO, userDTO)

        val savedRecord = if (exactMatch != null) {
            logger.info("Found exact match record, updating: ${exactMatch.id}")
            updateExistingRecord(exactMatch, recordDTO, userDTO)
        } else {
            val overlappingRecords = findOverlappingRecordsInDB(recordDTO, userDTO)

            if (overlappingRecords.isNotEmpty()) {
                logger.info("Found ${overlappingRecords.size} overlapping records, will replace them")

                unlinkFromRegistrationAndTrainingDay(overlappingRecords)
                recordRepository.deleteAll(overlappingRecords)
                logger.info("Deleted ${overlappingRecords.size} overlapping records")
            }

            logger.info("Creating new record")
            createNewRecord(recordDTO, userDTO)
        }

        linkToRegistrationAndTrainingDay(savedRecord)

        val result = recordMapper.toDto(savedRecord)
        logger.info("Sync completed for record: ${result.id}")
        return result
    }

    private fun findExactMatchRecord(recordDTO: RecordDTO, userDTO: UserDTO): Record? {
        return recordRepository.findByUserIdAndStartTimeAndEndTime(
            userId = userDTO.id!!,
            startTime = recordDTO.startTime!!,
            endTime = recordDTO.endTime!!
        ).firstOrNull()
    }

    private fun findOverlappingRecordsInDB(recordDTO: RecordDTO, userDTO: UserDTO): List<Record> {
        return recordRepository.findPotentialDuplicates(
            runnerId = userDTO.id!!,
            referenceStartTime = recordDTO.startTime!!,
            referenceEndTime = recordDTO.endTime!!
        ).filter { existingRecord ->
            hasTimeOverlap(
                existingRecord.startTime!!,
                existingRecord.endTime!!,
                recordDTO.startTime!!,
                recordDTO.endTime!!
            )
        }
    }

    private fun hasTimeOverlap(
        start1: LocalDateTime, end1: LocalDateTime,
        start2: LocalDateTime, end2: LocalDateTime
    ): Boolean {
        return start1 < end2 && start2 < end1
    }

    private fun unlinkFromRegistrationAndTrainingDay(records: List<Record>) {
        records.forEach { record ->
            try {
                record.registrations?.forEach { registration ->
                    registration.records = registration.records?.filter { it.id != record.id }
                    registrationRepository.save(registration)
                }

                val trainingDays = trainingDayRepository.findByRecordId(record.id!!)
                trainingDays.forEach { trainingDay ->
                    if(trainingDay.session!!.type != ETrainingSessionType.REST) {
                        trainingDay.record = null
                        trainingDay.status = ETrainingDayStatus.ACTIVE
                        trainingDay.completionPercentage = 0.0
                        trainingDayRepository.save(trainingDay)
                    }
                }
            } catch (e: Exception) {
                logger.error("Error unlinking record ${record.id}: ${e.message}")
            }
        }
    }

    private fun updateExistingRecord(existingRecord: Record, recordDTO: RecordDTO, userDTO: UserDTO): Record {
        logger.info("Updating existing record: ${existingRecord.id}")

        existingRecord.apply {
            steps = recordDTO.steps ?: steps
            distance = recordDTO.distance ?: distance
            avgSpeed = recordDTO.avgSpeed ?: avgSpeed
            heartRate = recordDTO.heartRate ?: heartRate
            source = recordDTO.source ?: source

            val savedRecordApproval = recordApprovalService.analyzeRecordApproval(recordDTO)
            val approvalEntity = recordApprovalRepository.findById(savedRecordApproval.id!!)
                .orElseThrow { RuntimeException("Approval not found") }
            approval = approvalEntity
        }

        return recordRepository.save(existingRecord)
    }

    private fun createNewRecord(recordDTO: RecordDTO, userDTO: UserDTO): Record {
        logger.info("Creating new record")

        val savedRecordApproval = recordApprovalService.analyzeRecordApproval(recordDTO)
        val approvalEntity = recordApprovalRepository.findById(savedRecordApproval.id!!)
            .orElseThrow { RuntimeException("Approval not found") }

        val record = recordMapper.toEntity(recordDTO).apply {
            approval = approvalEntity
            user = userMapper.toEntity(userDTO)
        }

        return recordRepository.save(record)
    }

    private fun linkToRegistrationAndTrainingDay(record: Record) {
        val userId = record.user?.id ?: throw RecordException("User ID is required")
        val startTime = record.startTime ?: throw RecordException("Start time is required")
        val endTime = record.endTime ?: throw RecordException("End time is required")

        val registrations = registrationRepository.findValidRegistrationsByUserIdAndRecordTimeRange(
            userId = userId,
            startTime = startTime,
            endTime = endTime
        )

        registrations.forEach { registration ->
            registration.records = (registration.records ?: emptyList()) + record
            registrationRepository.save(registration)
        }

        val trainingDay = trainingDayRepository.findByUserIdAndDateTimeRange(
            userId = userId,
            start = startTime,
            end = endTime
        ).firstOrNull()

        trainingDay?.let {
            if (it.record == null) {
                it.record = record
                it.completionPercentage = record.distance?.let { d -> (d / 5.0 * 100).coerceIn(0.0, 100.0) } ?: 0.0 // Giả định mục tiêu 5km
                it.status = when {
                    it.completionPercentage!! >= 100.0 -> ETrainingDayStatus.COMPLETED
                    it.completionPercentage!! > 0.0 -> ETrainingDayStatus.PARTIALLY_COMPLETED
                    else -> ETrainingDayStatus.ACTIVE
                }
                trainingDayRepository.save(it)
            } else {
                logger.warn("TrainingDay ${it.id} already has a Record, cannot assign another.")
                throw IllegalStateException("TrainingDay ${it.id} đã có Record, không thể gán thêm.")
            }
        }
    }
}
