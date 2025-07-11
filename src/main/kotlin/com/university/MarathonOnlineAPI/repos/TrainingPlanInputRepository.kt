package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Record
import com.university.MarathonOnlineAPI.entity.TrainingPlanInput
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrainingPlanInputRepository : JpaRepository<TrainingPlanInput, Long> {

}
