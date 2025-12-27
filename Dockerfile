# ========== Stage 1: Build ==========
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle 캐시 경로 명시
ENV GRADLE_USER_HOME=/root/.gradle

# 1. Gradle Wrapper 실행에 필요한 파일 먼저 복사
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

# 2. 의존성 정의 파일 먼저 복사
COPY build.gradle settings.gradle ./

# 3. 의존성 다운로드 (소스 없이 실행)
RUN ./gradlew --no-daemon dependencies

# 4. 소스 코드 복사
COPY src src

# 5. 실제 빌드
RUN ./gradlew --no-daemon assemble -x test

# ========== Stage 2: Run ==========
FROM eclipse-temurin:21-jre

WORKDIR /app

# 보안: non-root 유저
RUN useradd -m spring
USER spring

# builder 단계에서 만들어진 JAR만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
