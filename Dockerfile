# Java 21 OpenJDK 기반 경량 이미지 사용
FROM eclipse-temurin:21-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 이미 빌드된 JAR 파일을 직접 복사
COPY build/libs/memory-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 포트 설정
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
