
# Flyway 마이그레이션 리팩토링 가이드

## 📋 목차
1. [변경 사항 개요](#변경-사항-개요)
2. [주요 변경 내용](#주요-변경-내용)
3. [데이터베이스 변경 방법](#데이터베이스-변경-방법)
5. [환경별 설정](#환경별-설정)

---

## 변경 사항 개요

### 목적
- **프로덕션 안정성 향상**: JPA의 `ddl-auto: update`를 제거하고 Flyway로 스키마 버전 관리
- **스키마 버전 관리**: 모든 DDL/DML 변경을 SQL 마이그레이션 파일로 관리
- **환경 분리**: 개발 환경과 운영 환경의 데이터를 명확히 분리

### 변경 전후 비교

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 스키마 관리 | JPA `ddl-auto: update` | Flyway SQL 마이그레이션 |
| 초기 데이터 | Java `DataInitializer`, `CountryInitializer` | SQL `V2__insert_basic_data.sql`, `V3__insert_countries.sql` |
| 테스트 데이터 | Java 코드 | SQL `V4__test_user.sql` (개발 전용) |
| 스키마 검증 | 자동 생성 | `ddl-auto: validate` + Flyway 검증 |

---

## 주요 변경 내용

### 1. 의존성 추가 (`build.gradle`)

```gradle
dependencies {
    implementation 'org.flywaydb:flyway-core'
    runtimeOnly 'org.flywaydb:flyway-mysql'
}
```

### 2. JPA 설정 변경 (`application.yml`)

**변경 전:**
```yaml
jpa:
  hibernate:
    ddl-auto: update  # 자동 스키마 생성
```

**변경 후:**
```yaml
jpa:
  hibernate:
    ddl-auto: validate  # 스키마 검증만 수행
  properties:
    hibernate:
      physical_naming_strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

flyway:
  enabled: true
  baseline-on-migrate: false
  validate-on-migrate: true
  locations:
    - classpath:db/migration
    - classpath:db/migration-dev  # 개발 환경 전용
```

### 3. Flyway 마이그레이션 파일 구조

```
src/main/resources/db/
├── migration/              # 프로덕션 + 개발 환경 공통
│   ├── V1__init.sql        # DDL (테이블 생성)
│   ├── V2__insert_basic_data.sql  # 기초 데이터 (테마, 감정, 날씨 등)
│   └── V3__insert_countries.sql  # 국가 데이터 (245개)
└── migration-dev/          # 개발 환경 전용
    └── V4__test_user.sql   # 테스트 사용자 데이터
```

### 4. 엔티티 매핑 명시화

모든 엔티티에 명시적 매핑 추가:

```java
@Entity
@Table(name = "Diary")  // 테이블명 명시
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID 생성 전략 명시
    @Column(name = "diaryId")  // 컬럼명 명시 (camelCase)
    private Long id;
    
    @Lob
    @Column(name = "content", nullable = false, length = 88)
    private String content;  // TINYTEXT로 매핑
}
```

**변경된 엔티티 목록:**
- `User`, `Trip`, `Diary`, `DiaryImage`
- `TripTheme`, `BoardTheme`, `Emotion`, `Weather`
- `Country`, `VisitedCountry`, `Setting`
- `Board`, `BoardStickerMap`, `AlarmHistory`
- `Sticker`

### 5. 데이터 초기화 클래스 비활성화

**변경 전:**
```java
@Component  // 자동 실행
public class DataInitializer implements CommandLineRunner {
    // ...
}
```

**변경 후:**
```java
// @Component - 비활성화: Flyway 마이그레이션으로 대체됨
public class DataInitializer implements CommandLineRunner {
    // ...
}
```

**비활성화된 클래스:**
- `DataInitializer.java`
- `CountryInitializer.java`

### 6. 컬럼명 및 테이블명 통일

- **컬럼명**: 모든 컬럼을 `camelCase`로 통일 (예: `userId`, `createdAt`)
- **테이블명**: 엔티티 클래스명과 일치하도록 변경 (예: `User`, `Diary`, `TripTheme`)

---

## 데이터베이스 변경 방법

### 새로운 마이그레이션 파일 생성

1. **파일명 규칙**: `V{버전}__{설명}.sql`
   - 예: `V5__add_user_email_column.sql`
   - 버전은 순차적으로 증가 (V1, V2, V3...)

2. **파일 위치:**
   - 프로덕션 포함: `src/main/resources/db/migration/`
   - 개발 전용: `src/main/resources/db/migration-dev/`

3. **마이그레이션 작성 예시:**

```sql
-- V5__add_user_email_column.sql
ALTER TABLE `User` 
ADD COLUMN `email` VARCHAR(255) AFTER `kakaoId`;

-- 인덱스 추가
CREATE INDEX `idx_user_email` ON `User` (`email`);
```

### 마이그레이션 실행

**자동 실행:**
- 애플리케이션 시작 시 Flyway가 자동으로 미적용 마이그레이션 실행

**수동 실행 (선택사항):**
```bash
./gradlew flywayMigrate
```

### 마이그레이션 롤백

⚠️ **주의**: Flyway는 기본적으로 롤백을 지원하지 않습니다.

**롤백이 필요한 경우:**
1. 새로운 마이그레이션 파일로 롤백 SQL 작성
   ```sql
   -- V6__rollback_email_column.sql
   ALTER TABLE `User` DROP COLUMN `email`;
   ```

2. 또는 데이터베이스를 이전 상태로 복원 (백업 필요)

### 이미 적용된 마이그레이션 수정 금지

❌ **절대 하지 말 것:**
- 이미 적용된 마이그레이션 파일(V1, V2, V3...) 수정
- Checksum 불일치로 인한 오류 발생

✅ **올바른 방법:**
- 새로운 버전의 마이그레이션 파일로 변경사항 추가

---

## 환경별 설정

### 개발 환경 (`application.yml`)

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: false
    validate-on-migrate: true
    locations:
      - classpath:db/migration      # 공통 마이그레이션
      - classpath:db/migration-dev   # 개발 전용 (테스트 데이터)
```

**포함되는 마이그레이션:**
- V1: DDL (테이블 생성)
- V2: 기초 데이터
- V3: 국가 데이터
- V4: 테스트 사용자 (개발 전용)

### 운영 환경 (`application-prod.yml`)

```yaml
spring:
  config:
    activate:
      on-profile: prod
  
  flyway:
    enabled: true
    baseline-on-migrate: true  # 기존 DB에 Flyway 적용 시 필요
    validate-on-migrate: true
    locations: classpath:db/migration  # 개발 전용 제외
```

**포함되는 마이그레이션:**
- V1: DDL (테이블 생성)
- V2: 기초 데이터
- V3: 국가 데이터
- ❌ V4: 테스트 사용자 (제외됨)

### 프로파일 활성화

**로컬 개발:**
```bash
# 기본 프로파일 (application.yml 사용)
./gradlew bootRun
```

**운영 배포:**
```bash
# prod 프로파일 활성화
java -jar app.jar --spring.profiles.active=prod
```

---

## 체크리스트

### 새로운 스키마 변경 시

- [ ] 마이그레이션 파일명이 `V{버전}__{설명}.sql` 형식인가?
- [ ] 버전 번호가 기존 마이그레이션보다 큰가?
- [ ] 개발 전용 데이터인가? → `migration-dev/` 사용
- [ ] 엔티티에 `@Table`, `@Column` 매핑이 명시되어 있는가?
- [ ] `@GeneratedValue(strategy = GenerationType.IDENTITY)` 설정되어 있는가?
- [ ] 로컬에서 테스트 후 커밋했는가?

### 배포 전 확인

- [ ] `application-prod.yml`에 `baseline-on-migrate: true` 설정되어 있는가?
- [ ] 운영 환경에서 테스트 데이터 마이그레이션이 제외되는가?
- [ ] 데이터베이스 백업을 수행했는가?


**마지막 업데이트**: 2026-02-03
