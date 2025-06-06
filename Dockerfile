# Giai đoạn build
FROM gradle:7.6.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# Giai đoạn runtime
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app

# Cài đặt Python 3.10 và các gói cần thiết
RUN apt-get update && \
    apt-get install -y wget build-essential zlib1g-dev libncurses5-dev \
    libgdbm-dev libnss3-dev libssl-dev libreadline-dev libffi-dev libsqlite3-dev \
    python3-pip python3-dev && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY requirements.txt /app/
RUN pip3 install -r /app/requirements.txt

# Cài đặt các gói Python
RUN pip3 install pandas numpy scikit-learn

# Tạo thư mục pythonScripts và sao chép script
RUN mkdir -p /app/pythonScripts
COPY pythonScripts/ /app/pythonScripts/

# Đảm bảo các script Python có quyền thực thi phù hợp
RUN chmod +x /app/pythonScripts/*.py

# Thiết lập thư mục user_history để lưu trữ dữ liệu
RUN mkdir -p /app/user_history
ENV USER_HISTORY_PATH=/app/user_history
ENV PYTHON_PATH=python3

# Sao chép file JAR của ứng dụng
COPY --from=build /app/build/libs/MarathonOnlineAPI-0.0.1-SNAPSHOT.jar /app/MarathonOnlineAPI.jar

# Kiểm tra cài đặt Python và các gói
RUN python3 --version && pip3 list

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/MarathonOnlineAPI.jar"]