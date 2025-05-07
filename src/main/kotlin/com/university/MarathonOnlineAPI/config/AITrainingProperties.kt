package com.university.MarathonOnlineAPI.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "ai-training")
data class AITrainingProperties(
    var api: String = ""
)