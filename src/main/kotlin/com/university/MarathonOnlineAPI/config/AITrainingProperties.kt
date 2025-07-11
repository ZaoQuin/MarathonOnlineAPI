package com.university.MarathonOnlineAPI.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai-training")
data class AITrainingProperties(
    var api: String = ""
)