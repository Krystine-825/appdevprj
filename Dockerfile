# ================================
# 1️⃣ Build (Build ra file *.jar trong target)
# ================================
# Image maven 3.9.9 có eclipse project chạy jdk 21
FROM maven:3.9.9-eclipse-temurin-21 AS builder
# Tạo thư mục app trên container
WORKDIR /app

# Cache dependencies - sao lưu dependencies
COPY pom.xml ./
# Chạy lệnh resolve dependencies
RUN mvn dependency:resolve dependency:resolve-plugins

# Copy project files
#COPY . ./
COPY src ./src
# Chạy lệnh build
RUN mvn clean package -DskipTests


# ================================
# 2️⃣ Chạy file jar
# ================================
# Image eclipse project chạy jdk 21
FROM eclipse-temurin:21-jre
# Vào thư mục app trên container
WORKDIR /app
# Copy file *.jar vào file app.jar
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8080 for Spring Boot
EXPOSE 8080

# Set the default command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]

## Build image prj
# docker build -t krystine825/expense_tracker:latest .
## Push image from dev machine to repon
# docker push krystine825/expense_tracker:latest

## C1
## Pull image from repon to server machine
# docker pull krystine825/expense_tracker:latest
## Run image
# docker run --name expense_tracker -d –p 8080:8080 --rm krystine825/expense_tracker:latest

## C2
## Tạo docker-compose

#services:
#  expense_tracker:
#    container_name: expense_tracker
#    image: krystine825/expense_tracker:latest
#    hostname: expense_tracker.abcxyz
#    ports:
#      - 8080:8080
#    restart: always
#    networks:
#      - expense_tracker
#    environment:
#      - TZ:"Asia/Bangkok"
#networks:
#  expense_tracker:

## docker compose -p expense_tracker -f <đường dẫn>/docker-compose.expense_tracker.yaml up -d
## với docker-compose.expense_tracker.yaml - là tên file compose - có thể thay bằng đường dẫn đến tên file