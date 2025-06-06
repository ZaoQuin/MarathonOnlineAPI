# Giai đoạn build
FROM gradle:7.6.0-jdk17 AS build
WORKDIR /app
COPY . .
# Verify gradlew exists and is executable
RUN ls -la && test -f gradlew && chmod +x gradlew
# Run Gradle build with stacktrace for detailed error output
RUN ./gradlew bootJar --no-daemon --stacktrace

# Giai đoạn runtime
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app

# Install Python 3.10 and necessary packages
RUN apt-get update && \
    apt-get install -y python3 python3-pip python3-dev && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY requirements.txt /app/
RUN pip3 install -r requirements.txt
RUN pip3 install pandas numpy scikit-learn

# Create pythonScripts directory and copy scripts
RUN mkdir -p /app/pythonScripts
COPY pythonScripts/ /app/pythonScripts/
RUN chmod +x /app/pythonScripts/*.py

# Set up user_history directory
RUN mkdir -p /app/user_history
ENV USER_HISTORY_PATH=/app/user_history
ENV PYTHON_PATH=python3

# Copy the JAR from the build stage
COPY --from=build /app/build/libs/MarathonOnlineAPI-0.0.1-SNAPSHOT.jar /app/MarathonOnlineAPI.jar

# Verify Python installation
RUN python3 --version && pip3 list

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/MarathonOnlineAPI.jar"]