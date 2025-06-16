package com.university.MarathonOnlineAPI

import com.university.MarathonOnlineAPI.config.JwtProperties
import com.university.MarathonOnlineAPI.config.VnPayProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.*

@SpringBootApplication
@EnableConfigurationProperties(value = [JwtProperties::class, VnPayProperties::class])
@EnableScheduling
class MarathonOnlineApiApplication

fun main(args: Array<String>) {
	TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
	runApplication<MarathonOnlineApiApplication>(*args)
}
