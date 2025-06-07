package com.university.MarathonOnlineAPI.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class TimezoneConfig {
    @PostConstruct
    fun setTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
    }
}