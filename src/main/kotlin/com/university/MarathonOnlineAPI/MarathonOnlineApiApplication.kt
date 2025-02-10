package com.university.MarathonOnlineAPI

import com.university.MarathonOnlineAPI.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
@EnableScheduling
class MarathonOnlineApiApplication

fun main(args: Array<String>) {
	runApplication<MarathonOnlineApiApplication>(*args)
}
