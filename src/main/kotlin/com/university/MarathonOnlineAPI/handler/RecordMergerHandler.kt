package com.university.MarathonOnlineAPI.handler

import com.university.MarathonOnlineAPI.dto.CreateRecordRequest
import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.dto.UserDTO
import com.university.MarathonOnlineAPI.entity.Record
import com.university.MarathonOnlineAPI.entity.ERecordSource
import com.university.MarathonOnlineAPI.mapper.RecordMapper
import com.university.MarathonOnlineAPI.repos.RecordRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
class RecordMergerHandler(
    private val recordRepository: RecordRepository,
    private val recordMapper: RecordMapper
) {
    private val logger = LoggerFactory.getLogger(RecordMergerHandler::class.java)

    @Transactional
    fun mergeRecords(newRecords: List<CreateRecordRequest>, user: UserDTO): List<RecordDTO> {
        val resultRecords = mutableListOf<RecordDTO>()

        for (newRecord in newRecords) {
            logger.info("Processing new record: startTime=${newRecord.startTime}, endTime=${newRecord.endTime}")

            val heartRate = if (newRecord.heartRate == 0.0) null else newRecord.heartRate
            val recordDTO = RecordDTO(
                steps = newRecord.steps,
                distance = newRecord.distance,
                avgSpeed = newRecord.avgSpeed,
                timeTaken = Duration.between(newRecord.startTime, newRecord.endTime).seconds,
                heartRate = heartRate,
                startTime = newRecord.startTime,
                endTime = newRecord.endTime,
                source = ERecordSource.THIRD,
                user = user
            )

            val overlappingRecords = findOverlappingRecords(user.id!!, newRecord.startTime!!, newRecord.endTime!!)

            if (overlappingRecords.isNotEmpty()) {
                logger.info("Found ${overlappingRecords.size} overlapping records")
                val mergedRecord = createMergedRecord(overlappingRecords, recordDTO)

                recordRepository.deleteAll(overlappingRecords)
                logger.info("Deleted ${overlappingRecords.size} overlapping records")

                resultRecords.add(mergedRecord)
            } else {
                logger.info("No overlapping records found, adding new record")
                resultRecords.add(recordDTO)
            }
        }

        return resultRecords
    }

    private fun findOverlappingRecords(userId: Long, newStartTime: LocalDateTime, newEndTime: LocalDateTime): List<Record> {
        // Tìm tất cả records có bất kỳ overlap nào về thời gian
        return recordRepository.findPotentialDuplicates(userId, newStartTime, newEndTime)
            .filter { existingRecord ->
                hasTimeOverlap(
                    existingRecord.startTime!!,
                    existingRecord.endTime!!,
                    newStartTime,
                    newEndTime
                )
            }
    }

    private fun hasTimeOverlap(
        start1: LocalDateTime, end1: LocalDateTime,
        start2: LocalDateTime, end2: LocalDateTime
    ): Boolean {
        // Kiểm tra xem 2 khoảng thời gian có overlap không
        return start1 < end2 && start2 < end1
    }

    private fun createMergedRecord(existingRecords: List<Record>, newRecord: RecordDTO): RecordDTO {
        val allRecords = existingRecords.map { recordMapper.toDto(it) } + newRecord

        // Tính toán thời gian merged (từ sớm nhất đến muộn nhất)
        val mergedStartTime = allRecords.minOf { it.startTime!! }
        val mergedEndTime = allRecords.maxOf { it.endTime!! }
        val mergedDuration = Duration.between(mergedStartTime, mergedEndTime).seconds

        // Chọn giá trị tốt nhất từ các records
        val bestDistance = selectBestValue(allRecords.map { it.distance }, ::selectMaxDistance)
        val bestSteps = selectBestValue(allRecords.map { it.steps }, ::selectMaxSteps)
        val bestHeartRate = selectBestValue(allRecords.map { it.heartRate }, ::selectAverageHeartRate)

        // Tính lại avg speed dựa trên merged time và distance
        val mergedAvgSpeed = if (bestDistance != null && mergedDuration > 0) {
            (bestDistance / mergedDuration) * 3.6 // Convert m/s to km/h
        } else {
            selectBestValue(allRecords.map { it.avgSpeed }, ::selectMaxSpeed)
        }

        logger.info("Created merged record: startTime=$mergedStartTime, endTime=$mergedEndTime, distance=$bestDistance")

        return RecordDTO(
            steps = bestSteps,
            distance = bestDistance,
            avgSpeed = mergedAvgSpeed,
            heartRate = bestHeartRate,
            startTime = mergedStartTime,
            endTime = mergedEndTime,
            timeTaken = mergedDuration,
            source = ERecordSource.MERGED,
            user = newRecord.user
        )
    }

    private fun <T> selectBestValue(values: List<T?>, selector: (List<T>) -> T?): T? {
        val nonNullValues = values.filterNotNull()
        return if (nonNullValues.isNotEmpty()) selector(nonNullValues) else null
    }

    private fun selectMaxDistance(distances: List<Double>): Double? {
        return if (distances.isNotEmpty()) distances.maxOrNull() else null
    }

    private fun selectMaxSteps(steps: List<Int>): Int? {
        return if (steps.isNotEmpty()) steps.maxOrNull() else null
    }

    private fun selectAverageHeartRate(heartRates: List<Double>): Double? {
        return if (heartRates.isNotEmpty()) heartRates.average() else null
    }

    private fun selectMaxSpeed(speeds: List<Double>): Double? {
        return if (speeds.isNotEmpty()) speeds.maxOrNull() else null
    }
}