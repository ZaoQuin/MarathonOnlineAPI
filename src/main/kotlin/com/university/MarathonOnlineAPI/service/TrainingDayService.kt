package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.RecordDTO
import com.university.MarathonOnlineAPI.dto.TrainingDayDTO

interface TrainingDayService {
    fun getCurrentTrainingDayByJwt(jwt: String): TrainingDayDTO
    fun saveRecordIntoTrainingDay(recordDTO: RecordDTO, jwt: String): TrainingDayDTO
}