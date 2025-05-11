# Sử dụng hình ảnh Java 21
FROM openjdk:21-jdk-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Sao chép tệp JAR vào container
COPY target/ExpensesTracker-0.0.1-SNAPSHOT.jar app.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]