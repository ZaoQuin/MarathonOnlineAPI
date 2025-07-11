package com.university.MarathonOnlineAPI.config

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CloudinaryConfig {

    @Value("\${cloudinary.cloud-name:}")
    private lateinit var cloudName: String

    @Value("\${cloudinary.api-key:}")
    private lateinit var apiKey: String

    @Value("\${cloudinary.api-secret:}")
    private lateinit var apiSecret: String

    @Bean
    fun cloudinary(): Cloudinary {

        if (cloudName.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
            throw IllegalArgumentException("Cloudinary configuration is missing. Please check your application.properties file.")
        }

        return Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
            )
        )
    }
}