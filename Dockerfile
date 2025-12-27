# ========== Stage 1: Build ==========
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle Wrapper 실행에 필요한 파일 먼저 복사
COPY gradlew .
COPY gradle gradle

# 프로젝트 전체 소스 복사
COPY . .

# 실행 권한 부여
RUN chmod +x gradlew

# Docker 내부에서 빌드 실행
RUN ./gradlew clean build -x test


# ========== Stage 2: Run ==========
FROM eclipse-temurin:21-jre

WORKDIR /app

# builder 단계에서 만들어진 JAR만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
