package com.university.MarathonOnlineAPI.service.impl

import com.university.MarathonOnlineAPI.mapper.TrainingPlanInputMapper
import com.university.MarathonOnlineAPI.repos.TrainingPlanInputRepository
import com.university.MarathonOnlineAPI.service.TrainingPlanInputService
import org.springframework.stereotype.Service

@Service
class TrainingPlanInputServiceImpl(
    private val trainingPlanInputRepository: TrainingPlanInputRepository,
    private val trainingPlanInputMapper: TrainingPlanInputMapper,
) : TrainingPlanInputService {
}