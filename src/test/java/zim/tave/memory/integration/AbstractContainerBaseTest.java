package zim.tave.memory.integration;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;

/**
 * 통합 테스트를 위한 공통 컨테이너 베이스 클래스
 * 
 * Shared Container 패턴을 사용하여 모든 통합 테스트가 하나의 MySQL 컨테이너를 공유합니다.
 * 
 * Best Practice:
 * - static으로 선언하여 한 번만 띄우기
 * - 로컬 환경에서만 withReuse(true)로 컨테이너 재사용 (CI에서는 비활성화)
 * - 모든 통합 테스트 베이스 클래스가 이를 상속받도록 구성
 * 
 * 성능 최적화:
 * - Spring Context는 각 테스트 클래스마다 로드되지만, 컨테이너는 공유됩니다
 * - 로컬 환경에서 컨테이너 재사용으로 테스트 실행 시간 단축
 * - CI 환경에서는 매번 깨끗한 컨테이너를 사용하여 보안 및 정책 문제 방지
 * 
 * 주의: webEnvironment는 MOCK으로 설정 (NONE으로 하면 SecurityConfig의 HttpSecurity 빈을 찾을 수 없음)
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Testcontainers
public abstract class AbstractContainerBaseTest {

    /**
     * 모든 통합 테스트가 공유하는 MySQL 컨테이너
     * static으로 선언하여 테스트 클래스 간에 한 번만 생성됩니다.
     * 
     * Shared Container 패턴:
     * - @Container 어노테이션이 자동으로 컨테이너를 시작합니다
     * - static 필드로 선언하여 JVM 전체에서 하나의 인스턴스만 생성됩니다
     * - withReuse()는 로컬 환경에서만 활성화하여 성능을 향상시킵니다
     * - CI 환경에서는 재사용하지 않아 보안 및 정책 문제를 방지합니다
     */
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_memory_db")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(isLocalEnvironment()) // 로컬 환경에서만 컨테이너 재사용
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(120))); // 포트가 열릴 때까지 대기 (타임아웃 증가)
    
    /**
     * 로컬 환경 여부를 확인합니다.
     * CI 환경에서는 컨테이너 재사용을 비활성화하여 보안 및 정책 문제를 방지합니다.
     * 
     * @return 로컬 환경이면 true, CI 환경이면 false
     */
    private static boolean isLocalEnvironment() {
        // GitHub Actions 등 CI 환경에서는 자동으로 CI=true 환경변수가 설정됩니다
        String ciEnv = System.getenv("CI");
        return !"true".equalsIgnoreCase(ciEnv);
    }

    /**
     * Spring Boot 테스트에서 동적으로 데이터소스 속성을 설정합니다.
     * Testcontainers가 생성한 컨테이너의 JDBC URL을 사용합니다.
     * 
     * 중요: Spring Context가 로드되기 전에 컨테이너가 완전히 준비되도록 보장합니다.
     * 
     * 실행 순서:
     * 1. @Container가 컨테이너를 시작합니다
     * 2. @DynamicPropertySource가 호출됩니다
     * 3. 컨테이너가 완전히 준비될 때까지 대기합니다
     * 4. Spring Context가 데이터소스 속성을 사용하여 로드됩니다
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // 컨테이너가 시작되도록 보장
        // @Container가 자동으로 시작하지만, 명시적으로 확인하여 안정성을 높입니다
        if (!mysql.isRunning()) {
            mysql.start();
        }
        
        // 컨테이너가 완전히 준비될 때까지 기다림
        // getJdbcUrl()은 컨테이너가 시작될 때까지 자동으로 대기하지만,
        // 실제 MySQL이 연결을 받을 준비가 될 때까지 추가로 대기합니다
        String jdbcUrl = mysql.getJdbcUrl();
        
        // MySQL이 실제로 연결을 받을 준비가 될 때까지 대기
        waitForContainerReady(jdbcUrl, mysql.getUsername(), mysql.getPassword());
        
        // Spring Boot의 데이터소스 속성 설정
        // Supplier를 사용하여 런타임에 값을 가져옵니다
        // UTC 타임존 설정 추가
        registry.add("spring.datasource.url", () -> jdbcUrl + (jdbcUrl.contains("?") ? "&serverTimezone=UTC" : "?serverTimezone=UTC"));
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQL8Dialect");
        
        // JPA 설정 최적화
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "false");
    }
    
    /**
     * 컨테이너가 실제로 연결을 받을 준비가 될 때까지 대기합니다.
     * 
     * MySQL 컨테이너가 시작되어 포트가 열렸더라도,
     * 실제로 데이터베이스 연결을 받을 준비가 되기까지 시간이 걸릴 수 있습니다.
     * 
     * @param jdbcUrl JDBC 연결 URL
     * @param username 데이터베이스 사용자명
     * @param password 데이터베이스 비밀번호
     * @throws IllegalStateException 최대 재시도 횟수 초과 시
     */
    private static void waitForContainerReady(String jdbcUrl, String username, String password) {
        int maxRetries = 60; // 최대 60초 대기 (타임아웃 증가)
        int retryCount = 0;
        long startTime = System.currentTimeMillis();
        
        while (retryCount < maxRetries) {
            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                // 연결 성공 시 준비 완료
                long elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println("MySQL container is ready after " + elapsedTime + "ms");
                return;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    throw new IllegalStateException(
                        "MySQL container is not ready after " + elapsedTime + "ms (" + maxRetries + " attempts). " +
                        "JDBC URL: " + jdbcUrl + 
                        ". Please check Docker Desktop is running and has enough resources.", e);
                }
                try {
                    Thread.sleep(1000); // 1초 대기
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for container", ie);
                }
            }
        }
    }
}

