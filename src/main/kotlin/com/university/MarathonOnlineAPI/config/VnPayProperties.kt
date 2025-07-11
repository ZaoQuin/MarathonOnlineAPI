package com.university.MarathonOnlineAPI.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "vnp")
data class VnPayProperties (
    val tmnCode: String,
    val hashSecret: String,
    val payUrl: String,
    val returnUrl: String
)