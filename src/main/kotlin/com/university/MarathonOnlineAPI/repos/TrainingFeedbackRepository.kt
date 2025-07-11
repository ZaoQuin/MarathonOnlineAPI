package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.TrainingFeedback
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface TrainingFeedbackRepository: JpaRepository<TrainingFeedback, Long> {
    @Query("SELECT tf FROM TrainingFeedback tf WHERE tf.trainingDay.id = :trainingDayId")
    fun findByTrainingDayId(@Param("trainingDayId") trainingDayId: Long): Optional<TrainingFeedback>

    fun existsByTrainingDayId(trainingDayId: Long): Boolean
}