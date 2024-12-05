import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")  // Cho phép các endpoint bắt đầu với /api/
            .allowedOrigins("http://localhost:3000")  // Chỉ cho phép origin từ localhost:3000
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Cho phép OPTIONS để xử lý preflight requests
            .allowedHeaders("Content-Type", "Authorization")  // Cho phép các header cụ thể
            .allowCredentials(true)  // Cho phép truyền thông tin xác thực (cookies, headers)
    }
}
