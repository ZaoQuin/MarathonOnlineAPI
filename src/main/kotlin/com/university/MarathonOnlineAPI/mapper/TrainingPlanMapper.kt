package com.university.MarathonOnlineAPI.mapper

import com.university.MarathonOnlineAPI.dto.TrainingPlanDTO
import com.university.MarathonOnlineAPI.entity.ETrainingDayStatus
import com.university.MarathonOnlineAPI.entity.TrainingPlan
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class TrainingPlanMapper(
    private val modelMapper: ModelMapper,
    private val trainingDayMapper: TrainingDayMapper,
): Mapper<TrainingPlanDTO, TrainingPlan> {
    override fun toDto(entity: TrainingPlan): TrainingPlanDTO {
        val dto = modelMapper.map(entity, TrainingPlanDTO::class.java)
        dto.trainingDays = entity.trainingDays.map { trainingDayMapper.toDto(it) }

        val completedDays = entity.trainingDays.count {
            it.status == ETrainingDayStatus.COMPLETED ||
                    it.status == ETrainingDayStatus.PARTIALLY_COMPLETED }
        val totalDays = entity.input!!.trainingWeeks!! * 7
        val remainingDays = totalDays - entity.trainingDays.count {
            it.status != ETrainingDayStatus.ACTIVE }
        val totalDistance = entity.trainingDays.mapNotNull { it.session?.distance }.sum()
        val progress = if (totalDays > 0) (completedDays.toDouble() / totalDays) * 100 else 0.0

        dto.completedDays = completedDays
        dto.remainingDays = remainingDays
        dto.totalDistance = totalDistance
        dto.progress = progress

        return dto
    }
    override fun toEntity(dto: TrainingPlanDTO): TrainingPlan {
        return modelMapper.map(dto, TrainingPlan::class.java)
    }
}