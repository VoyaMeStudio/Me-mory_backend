package zim.tave.memory.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 통합 테스트를 위한 공통 컨테이너 베이스 클래스
 * 
 * Shared Container 패턴을 사용하여 모든 통합 테스트가 하나의 MySQL 컨테이너를 공유합니다.
 * 
 * Best Practice:
 * - static으로 선언하여 한 번만 띄우기
 * - withReuse(true) 설정으로 컨테이너 재사용
 * - 모든 통합 테스트 베이스 클래스가 이를 상속받도록 구성
 * 
 * 성능 최적화:
 * - Spring Context는 각 테스트 클래스마다 로드되지만, 컨테이너는 공유됩니다
 * - Testcontainers 설정 파일(testcontainers.properties)에서 reuse 활성화
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public abstract class AbstractContainerBaseTest {

    /**
     * 모든 통합 테스트가 공유하는 MySQL 컨테이너
     * static으로 선언하여 테스트 클래스 간에 한 번만 생성됩니다.
     */
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_memory_db")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true); // 컨테이너 재사용 활성화

    /**
     * Spring Boot 테스트에서 동적으로 데이터소스 속성을 설정합니다.
     * Testcontainers가 생성한 컨테이너의 JDBC URL을 사용합니다.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQL8Dialect");
    }
}

