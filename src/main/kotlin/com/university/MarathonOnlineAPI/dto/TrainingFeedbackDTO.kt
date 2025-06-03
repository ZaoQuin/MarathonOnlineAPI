package com.university.MarathonOnlineAPI.dto

import com.university.MarathonOnlineAPI.entity.EDifficultyRating
import com.university.MarathonOnlineAPI.entity.EFeelingRating

data class TrainingFeedbackDTO (
    var id: Long? = null,
    var difficultyRating: EDifficultyRating?= null,
    var feelingRating: EFeelingRating?= null,
    var notes: String? = null,
)
