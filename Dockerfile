# Build stage
FROM gradle:7.6.0-jdk17 AS build
WORKDIR /app
COPY . .                          # Copy toàn bộ mã nguồn vào container
RUN ./gradlew bootJar --no-daemon # Build file JAR bằng Gradle

# Runtime stage
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/MarathonOnlineAPI-0.0.1-SNAPSHOT.jar /app/MarathonOnlineAPI.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/MarathonOnlineAPI.jar"]
