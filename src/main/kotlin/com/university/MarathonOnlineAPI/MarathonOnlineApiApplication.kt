package com.university.MarathonOnlineAPI

import com.university.MarathonOnlineAPI.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
class MarathonOnlineApiApplication

fun main(args: Array<String>) {
	runApplication<MarathonOnlineApiApplication>(*args)
}
