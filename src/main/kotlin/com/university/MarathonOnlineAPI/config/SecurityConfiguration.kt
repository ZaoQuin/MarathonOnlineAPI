package com.university.MarathonOnlineAPI.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    private val authenticationProvider: AuthenticationProvider
) : WebMvcConfigurer {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): DefaultSecurityFilterChain =
        http.cors {}
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/v1/auth", "/api/v1/auth/refresh",
                        "/api/v1/user/check-email", "/api/v1/user/check-username",
                        "/api/v1/user/update-password", "/api/v1/record/user/*/history",
                        "/api/v1/payment/vnpay-return", "/error")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/logout")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/v1/user")
                    .permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/v1/user")
                    .authenticated()
                    .requestMatchers("/api/v1/user**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .fullyAuthenticated()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()

    // Cấu hình CORS cho phép truy cập từ localhost:3000 (React app)
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // Chỉ cho phép CORS cho các API
            .allowedOrigins("https://login-admin-page.onrender.com") // React frontend chạy trên localhost:3000
            .allowedMethods("GET", "POST", "PUT", "DELETE") // Các phương thức HTTP được phép
            .allowedHeaders("*") // Cho phép tất cả headers
            .allowCredentials(true)
    }
}