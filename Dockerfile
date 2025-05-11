# Build stage
FROM gradle:7.6.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app

# Install Python 3.10 and create a symlink to 'python'
RUN apt-get update && \
    apt-get install -y wget build-essential zlib1g-dev libncurses5-dev \
    libgdbm-dev libnss3-dev libssl-dev libreadline-dev libffi-dev libsqlite3-dev && \
    wget -qO- https://www.python.org/ftp/python/3.10.13/Python-3.10.13.tgz | tar xvz && \
    cd Python-3.10.13 && \
    ./configure --enable-optimizations && \
    make -j$(nproc) && \
    make altinstall && \
    cd .. && \
    rm -rf Python-3.10.13 && \
    ln -s /usr/local/bin/python3.10 /usr/local/bin/python && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create pythonScripts directory
# Create pythonScripts directory and copy scripts
RUN mkdir -p /app/pythonScripts
COPY pythonScripts/ /app/pythonScripts/
RUN chmod +x /app/pythonScripts/record_validator.py
ENV PYTHON_PATH=/usr/local/bin/python

# Verify Python installation
RUN python --version && python3.10 --version

# Copy application JAR file
COPY --from=build /app/build/libs/MarathonOnlineAPI-0.0.1-SNAPSHOT.jar /app/MarathonOnlineAPI.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/MarathonOnlineAPI.jar"]