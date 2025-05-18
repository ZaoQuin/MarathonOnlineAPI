package com.university.MarathonOnlineAPI.view

import com.university.MarathonOnlineAPI.entity.ETrainingPlanStatus
import java.time.LocalDateTime

interface SingleTrainingPlanView {
    fun getId(): Long
    fun getName(): String
    fun getStartDate(): LocalDateTime
    fun getEndDate(): LocalDateTime
    fun getStatus(): ETrainingPlanStatus
}