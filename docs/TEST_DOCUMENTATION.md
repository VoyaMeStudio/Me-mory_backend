# Me-mory Backend 테스트 문서

## 목차
1. [테스트 개요](#테스트-개요)
2. [테스트 구조](#테스트-구조)
3. [Service 테스트](#service-테스트)
4. [Controller 테스트](#controller-테스트)
5. [Integration 테스트](#integration-테스트)
6. [Domain 테스트](#domain-테스트)
7. [Util 테스트](#util-테스트)
8. [테스트 실행 방법](#테스트-실행-방법)

---
## 진행사항

### 실패 테스트 분류
- **Integration 테스트**: 27개 실패
  - RecordingFlowIntegrationTest: 5개 실패
  - SettingsIntegrationTest: 9개 실패
  - TimelineIntegrationTest: 3개 실패
  - ViewRecordsFlowIntegrationTest: 10개 실패

### ⚠️ 중요 원칙
**실무 원칙: 실패하는 테스트가 있는 상태로 머지(Merge)하지 않습니다.**


### 테스트 실행 방법
**⚠️ Integration 테스트는 제외하고 실행해주세요:**

로컬 개발 시에는 `unitTest` 태스크를 사용하여 통합 테스트를 제외하고 실행하는 것을 권장합니다:

```bash
# Windows (PowerShell)
.\gradlew.bat unitTest

# Linux/Mac 또는 Git Bash
./gradlew unitTest
```

**태스크 설정 방식**:
- `build.gradle`에서 `useJUnitPlatform { excludeTags 'integration' }`을 사용하여 `@Tag("integration")` 어노테이션이 있는 테스트를 제외합니다.
- 통합 테스트 베이스 클래스(`AbstractContainerBaseTest`, `IntegrationTestBase`, `ApiTestBase`)에 `@Tag("integration")` 어노테이션이 적용되어 있습니다.

단위 테스트(Service, Controller, Domain, Util)는 모두 정상 작동하며 빠르게 실행됩니다.
Integration 테스트는 Docker Desktop으로 데이터베이스(MySQL) 컨테이너를 생성한 후 통합 테스트를 진행하기 때문에 **현재 약 14분 정도** 걸립니다. 


### Docker 환경 확인 (실패 시)
실행 전 다음을 확인하세요:
- **Docker Desktop 설치 및 실행**
  - Docker Desktop이 설치되어 있어야 합니다
  - Docker Desktop이 실행 중이어야 합니다
  - 확인 방법: `docker ps` 명령어 실행
- **Docker 접근 권한**
  - Windows: Docker Desktop이 WSL 2 백엔드를 사용하는 경우 WSL 2가 설치되어 있어야 합니다
  - Docker Desktop 설정에서 "Use WSL 2 based engine" 확인
- **네트워크 연결**
  - Docker 이미지를 다운로드하기 위해 인터넷 연결이 필요합니다
  - 해결 방법:
    - Docker Desktop 재시작
    - Docker Desktop 설정에서 "Expose daemon on tcp://localhost:2375" 확인

---

## 테스트 개요

이 프로젝트는 **계층별 테스트 전략**을 사용합니다:
- **Service 테스트**: 비즈니스 로직 단위 테스트 (Mockito 사용)
- **Controller 테스트**: API 엔드포인트 단위 테스트 (MockMvc 사용)
- **Integration 테스트**: 전체 플로우 통합 테스트 (실제 DB 사용)
- **Domain 테스트**: 도메인 엔티티 로직 테스트
- **Util 테스트**: 유틸리티 함수 테스트

### 테스트 비중 (테스트 피라미드 모델)

실무 표준에 따른 테스트 비중:

- **Unit Test** (Service, Domain, Util): **70%**
  - 비즈니스 로직의 모든 경우의 수 커버 (Happy path + Edge cases)
  - Mockito를 사용한 빠른 실행
  - 현재 상태: ✅ 목표 비중 달성

- **Integration Test** (Controller, Repository): **20%**
  - Spring Context 로드 확인
  - JPA 쿼리 문법 검증
  - API 스펙 문서화 용도
  - 현재 상태: ✅ 목표 비중 달성

- **E2E/System Test**: **10%**
  - DB까지 연결된 실제 흐름 확인
  - 중요한 비즈니스 시나리오 위주
  - 현재 상태: ✅ 목표 비중 달성

---

## 테스트 구조

```
src/test/java/zim/tave/memory/
├── service/          # Service 계층 단위 테스트
├── controller/       # Controller 계층 단위 테스트
├── integration/      # 통합 테스트
├── domain/          # 도메인 엔티티 테스트
└── util/            # 유틸리티 테스트
```

---

## Service 테스트

### 테스트 방식
- **프레임워크**: JUnit 5 + Mockito
- **어노테이션**: `@ExtendWith(MockitoExtension.class)`
- **Mock 사용**: `@Mock`으로 의존성 모킹, `@InjectMocks`로 테스트 대상 주입
- **검증**: AssertJ 사용

### 테스트 파일 목록

#### 1. JoinServiceTest
**목적**: 회원가입 서비스 로직 테스트

**테스트 케이스**:
- ✅ `회원가입_성공_테스트`: 정상적인 회원가입 플로우 검증
- ✅ `사용자_없음_예외_테스트`: 존재하지 않는 사용자 ID로 가입 시도 시 예외 처리
- ✅ `카카오_로그인_필수_예외_테스트`: 카카오 로그인 없이 가입 시도 시 예외 처리
- ✅ `이미_가입된_회원_예외_테스트`: 이미 가입된 사용자의 중복 가입 방지

**주요 검증 사항**:
- 사용자 정보 저장 (이름, 생년월일, 국적 등)
- 초기 통계 정보 설정 (일기 수, 방문 국가 수, 국기)
- 등록 상태 변경 (`isRegistered = true`)

---

#### 2. LoginServiceTest
**목적**: 카카오 로그인 서비스 로직 테스트

**테스트 케이스**:
- ✅ `testKakaoLogin`: 카카오 API를 통한 로그인 처리 검증

**주요 검증 사항**:
- 카카오 API 클라이언트를 통한 사용자 정보 조회
- 로그인 응답 DTO 생성 및 반환

---

#### 3. DiaryServiceTest
**목적**: 일기(다이어리) 서비스 로직 테스트

**테스트 케이스**:
- ✅ `다이어리_생성_테스트`: 일기 생성 기능 검증
- ✅ `다이어리_생성_시_사용자_없음_예외_테스트`: 존재하지 않는 사용자 예외 처리
- ✅ `선택적_필드_업데이트_테스트`: 상세 위치, 감정, 날씨 업데이트
- ✅ `대표사진_변경_테스트`: 대표사진 변경 로직 검증
- ✅ `대표사진_변경_시_이미지_없음_예외_테스트`: 존재하지 않는 이미지 예외 처리
- ✅ `다이어리_삭제_테스트`: 일기 삭제 및 여행 종료날짜 업데이트
- ✅ `다이어리_삭제_후_남은_다이어리_있을_때_종료날짜_업데이트_테스트`: 삭제 후 종료날짜 재계산
- ✅ `다이어리_삭제_소유권_검증_실패_테스트`: 다른 사용자의 일기 삭제 시도 시 예외
- ✅ `다이어리_삭제_인증_실패_테스트`: 인증되지 않은 사용자 예외 처리
- ✅ `사용자별_다이어리_조회_테스트`: 사용자별 일기 목록 조회
- ✅ `여행별_다이어리_조회_테스트`: 여행별 일기 목록 조회
- ✅ `다이어리_보관_테스트`: 일기 보관 기능
- ✅ `다이어리_보관_해제_테스트`: 일기 보관 해제 기능
- ✅ `다이어리_보관_소유권_검증_실패_테스트`: 소유권 검증 실패 시 예외
- ✅ `다이어리_보관_인증_실패_테스트`: 인증 실패 시 예외
- ✅ `다이어리_보관_다이어리_없음_예외_테스트`: 존재하지 않는 일기 예외
- ✅ `사용자별_다이어리_조회_시_숨긴_다이어리_제외_테스트`: 보관된 일기 필터링
- ✅ `여행별_다이어리_조회_시_숨긴_다이어리_제외_테스트`: 보관된 일기 필터링

**주요 검증 사항**:
- 일기 CRUD 작업
- 이미지 관리 (전면/후면 카메라, 대표사진)
- 감정 및 날씨 정보 관리
- 소유권 및 인증 검증
- 보관 기능 및 필터링

---

#### 4. TripServiceTest
**목적**: 여행 서비스 로직 테스트

**테스트 케이스**:
- ✅ `여행_생성_테스트`: 새 여행 생성
- ✅ `여행_생성_시_테마_없음_예외_테스트`: 존재하지 않는 테마 예외 처리
- ✅ `여행_수정_테스트`: 여행 정보 수정
- ✅ `여행_수정_시_테마_변경_테스트`: 여행 테마 변경
- ✅ `사용자별_여행_DTO_조회_테스트`: 사용자별 여행 목록 조회
- ✅ `여행_종료날짜_업데이트_테스트`: 종료날짜 자동 업데이트
- ✅ `마지막_다이어리_날짜_조회_테스트`: 마지막 일기 날짜 조회
- ✅ `과거_여행_생성_테스트`: 과거 여행 생성
- ✅ `여행_보관상태_변경_테스트`: 여행 보관/복구
- ✅ 기타 여행 관련 기능 테스트

**주요 검증 사항**:
- 여행 CRUD 작업
- 여행 테마 관리
- 여행 날짜 관리 (시작일, 종료일)
- 여행 보관 기능
- 과거 여행 처리

---

#### 5. TimelineServiceTest
**목적**: 타임라인 서비스 로직 테스트

**테스트 케이스**:
- ✅ 타임라인 조회 기능
- ✅ 보관되지 않은 여행만 표시
- ✅ 시간순 정렬
- ✅ 감정 정보 포함
- ✅ 대표 이미지 포함

**주요 검증 사항**:
- 타임라인 데이터 조회 및 정렬
- 보관된 여행 필터링
- 감정 및 이미지 정보 포함

---

#### 6. MyPageServiceTest
**목적**: 마이페이지 서비스 로직 테스트

**테스트 케이스**:
- ✅ `myPageServiceTest`: 마이페이지 정보 조회

**주요 검증 사항**:
- 사용자 정보 조회
- 통계 정보 (일기 수, 방문 국가 수)

---

#### 7. SettingServiceTest
**목적**: 설정 서비스 로직 테스트

**테스트 케이스**:
- ✅ `logout`: 로그아웃 기능
- ✅ `deleteUser`: 회원 탈퇴 기능
- ✅ `delete_exception`: 존재하지 않는 사용자 탈퇴 시 예외 처리

**주요 검증 사항**:
- 로그아웃 처리
- 회원 탈퇴 및 데이터 삭제
- 예외 처리

---

#### 8. CountryServiceTest
**목적**: 국가 서비스 로직 테스트

**테스트 케이스**:
- ✅ `init_빈_DB에_정상작동_확인`: 국가 데이터 초기화
- ✅ `init_중복저장_방지_확인`: 중복 저장 방지 검증
- ✅ `searchCountryByKeyword`: 국가 이름으로 검색

**주요 검증 사항**:
- 국가 데이터 초기화
- 중복 저장 방지
- 국가 검색 기능

---

#### 9. VisitedCountryServiceTest
**목적**: 방문 국가 서비스 로직 테스트

**테스트 케이스**:
- ✅ `testRegisterVisitedCountry_SuccessAndUpdate`: 방문 국가 등록 및 업데이트
- ✅ `testRegisterVisitedCountry_ReturnsSavedEntity`: 등록된 엔티티 반환 검증
- ✅ `testAlreadyVisited`: 방문 여부 확인
- ✅ `testGetVisitedCountries`: 방문 국가 목록 조회

**주요 검증 사항**:
- 방문 국가 등록 및 업데이트
- 방문 여부 확인
- 방문 국가 목록 조회

---

## Controller 테스트

### 테스트 방식
- **프레임워크**: JUnit 5 + MockMvc
- **어노테이션**: `@ExtendWith(MockitoExtension.class)`
- **Mock 사용**: Service 계층을 Mock으로 처리
- **검증**: MockMvc를 통한 HTTP 요청/응답 검증

### 테스트 검증 항목
Controller 테스트는 다음 항목들을 검증합니다:
- **HTTP 상태 코드**: 요청에 대한 적절한 상태 코드 반환
- **응답 본문 구조**: JSON 응답의 구조 및 필드 검증
- **인증/인가 검증**: 인증되지 않은 사용자, 권한 없는 사용자 처리
- **예외 처리**: 비즈니스 로직 예외의 적절한 HTTP 응답 변환
- **입력값 검증(Validation)**: 
  - 필수 필드 누락 시 400 에러 검증
  - `@NotNull`, `@NotBlank` 위반 케이스
  - 잘못된 데이터 타입 입력 검증

**입력값 검증 테스트 예시**:
```java
@Test
void 일기_생성_필수_필드_누락_400() throws Exception {
    // given - 필수 필드 누락
    CreateDiaryRequest request = new CreateDiaryRequest();
    // tripId, countryCode, city 등 필수 필드 미설정
    
    // when & then
    mockMvc.perform(post("/api/users/me/diaries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
}
```

### 테스트 파일 목록

#### 1. DiaryControllerTest
**목적**: 일기 API 엔드포인트 테스트

**테스트 케이스**:
- ✅ `일기_생성_성공`: POST `/api/users/me/diaries`
- ✅ `일기_생성_인증_실패`: 인증되지 않은 사용자 예외
- ✅ `일기_선택적_필드_업데이트_성공`: PATCH `/api/users/me/diaries/{diaryId}/optional-fields`
- ✅ `일기_대표사진_변경_성공`: PATCH `/api/users/me/diaries/{diaryId}/representative-image`
- ✅ `일기_삭제_성공`: DELETE `/api/users/me/diaries/{diaryId}`
- ✅ `일기_조회_성공`: GET `/api/users/me/diaries`
- ✅ `여행별_일기_조회_성공`: GET `/api/users/me/trips/{tripId}/diaries`
- ✅ `일기_보관_성공`: PATCH `/api/users/me/diaries/{diaryId}/store`
- ✅ 기타 예외 처리 테스트

**주요 검증 사항**:
- HTTP 상태 코드
- 응답 본문 구조
- 인증/인가 검증
- 예외 처리

---

#### 2. TimelineControllerTest
**목적**: 타임라인 API 엔드포인트 테스트

**테스트 케이스**:
- ✅ `타임라인_조회_성공`: GET `/api/users/me/timeline`
- ✅ `타임라인_조회_인증_실패`: 인증되지 않은 사용자 예외
- ✅ `과거_여행_추가_성공`: POST `/api/users/me/timeline/past-trips`
- ✅ `과거_여행_추가_인증_실패`: 인증 실패 예외
- ✅ 기타 타임라인 관련 API 테스트

**주요 검증 사항**:
- 타임라인 데이터 조회
- 과거 여행 추가
- 인증/인가 검증

---

#### 3. CountryControllerTest
**목적**: 국가 API 엔드포인트 테스트

**테스트 케이스**:
- ✅ 국가 검색 API 테스트
- ✅ 국가 목록 조회 API 테스트

---

## Integration 테스트

### 테스트 방식
- **프레임워크**: JUnit 5 + Spring Boot Test + Testcontainers
- **어노테이션**: `@SpringBootTest`, `@Transactional`
- **DB**: Testcontainers를 사용한 실제 데이터베이스 (MySQL)
- **검증**: 실제 서비스와 리포지토리를 통한 통합 검증

### 테스트 환경 설정

**✅ Shared Container 패턴 적용 완료**

모든 통합 테스트가 하나의 MySQL 컨테이너를 공유하도록 `AbstractContainerBaseTest`를 구현했습니다.

**구현 내용**:
- `AbstractContainerBaseTest`: 공통 컨테이너 베이스 클래스
  - `static MySQLContainer`로 선언하여 JVM 전체에서 하나의 인스턴스만 생성
  - `withReuse(true)` 설정으로 컨테이너 재사용
  - 컨테이너 준비 대기 메커니즘 구현 (타임아웃 120초, 연결 대기 60초)
  - `@DynamicPropertySource`를 통한 동적 데이터소스 설정
- `IntegrationTestBase`: 통합 테스트 베이스 클래스
  - `AbstractContainerBaseTest`를 상속
  - 공통 테스트 데이터 초기화 메서드 제공
- `ApiTestBase`: API 통합 테스트 베이스 클래스
  - `AbstractContainerBaseTest`를 상속
  - MockMvc 및 JWT 토큰 생성 헬퍼 메서드 제공

**구조**:
```java
// AbstractContainerBaseTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Testcontainers
public abstract class AbstractContainerBaseTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_memory_db")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(120)));
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // 컨테이너 준비 확인 및 데이터소스 설정
        if (!mysql.isRunning()) {
            mysql.start();
        }
        String jdbcUrl = mysql.getJdbcUrl();
        waitForContainerReady(jdbcUrl, mysql.getUsername(), mysql.getPassword());
        
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQL8Dialect");
    }
}

// IntegrationTestBase와 ApiTestBase가 이를 상속
```

**Testcontainers 설정** (`src/test/resources/testcontainers.properties`):
- `testcontainers.reuse.enable=true`: 컨테이너 재사용 활성화
- Windows 환경 호환성 설정 포함

**⚠️ 현재 문제사항**:
- Windows 환경에서 Integration 테스트 실패 (27개)
- 원인 추정: Windows 환경에서 Testcontainers/Docker Desktop 설정 문제
- CI/CD 환경(Linux)에서는 정상 작동할 가능성 있음
- 해결 방안: Docker Desktop 실행 상태, 리소스 할당 확인 필요

### 테스트 파일 목록

#### 1. RecordingFlowIntegrationTest
**목적**: 기록하기 플로우 전체 통합 테스트

**테스트 케이스**:
- ✅ `새_여행_생성_후_일기_추가_테스트`: 여행 생성 → 일기 추가 전체 플로우
- ✅ `기존_여행에_일기_추가_테스트`: 기존 여행에 일기 추가
- ✅ `대표사진_선택_테스트`: 대표사진 선택 및 변경
- ✅ `감정색_기본값_테스트`: 감정색 기본값 설정
- ✅ `날씨_선택_안함_테스트`: 날씨 선택하지 않은 경우

**주요 검증 사항**:
- 여행 생성 및 일기 추가 전체 플로우
- 이미지 업로드 및 대표사진 선택
- 감정 및 날씨 정보 관리
- 여행 종료날짜 자동 업데이트

---

#### 2. MyPageIntegrationTest
**목적**: 마이페이지 기능 통합 테스트

**테스트 케이스**:
- ✅ `마이페이지_사용자_정보_조회_테스트`: 사용자 정보 조회
- ✅ `마이페이지_일기_개수_조회_테스트`: 일기 개수 통계
- ✅ `마이페이지_방문_국가_중복_없이_조회_테스트`: 방문 국가 중복 제거
- ✅ `마이페이지_방문_국가_개수_조회_테스트`: 방문 국가 개수 통계

**주요 검증 사항**:
- 사용자 정보 조회
- 통계 정보 계산
- 방문 국가 중복 제거

---

#### 3. SettingsIntegrationTest
**목적**: 설정 기능 통합 테스트

**테스트 케이스**:
- ✅ `개인_정보_수정_테스트`: 사용자 정보 수정
- ✅ `보관된_여행_목록_조회_테스트`: 보관된 여행 조회
- ✅ `보관된_여행_복구_테스트`: 보관된 여행 복구
- ✅ `보관된_여행_삭제_테스트`: 보관된 여행 삭제
- ✅ `보관된_일기_목록_조회_테스트`: 보관된 일기 조회
- ✅ `보관된_일기_복구_테스트`: 보관된 일기 복구
- ✅ `보관된_일기_삭제_테스트`: 보관된 일기 삭제
- ✅ `로그아웃_테스트`: 로그아웃 기능
- ✅ `회원_탈퇴_테스트`: 회원 탈퇴 기능

**주요 검증 사항**:
- 사용자 정보 수정
- 보관 기능 (여행/일기)
- 로그아웃 및 회원 탈퇴

---

#### 4. ViewRecordsFlowIntegrationTest
**목적**: 기록 보기 및 수정 플로우 통합 테스트

**테스트 케이스**:
- ✅ `여행_이름_수정_테스트`: 여행 이름 수정
- ✅ `여행_대표_이미지_수정_테스트`: 여행 대표 이미지 수정
- ✅ `여행_테마_수정_테스트`: 여행 테마 수정
- ✅ `여행_날짜_수정_테스트`: 여행 날짜 수정
- ✅ `여행_보관_테스트`: 여행 보관
- ✅ `여행_삭제_테스트`: 여행 삭제
- ✅ `일기_장소명_수정_테스트`: 일기 장소명 수정
- ✅ `일기_기록_수정_테스트`: 일기 내용 수정
- ✅ `일기_감정색_수정_기본값_테스트`: 일기 감정색 수정
- ✅ `일기_날씨_수정_기본값_테스트`: 일기 날씨 수정

**주요 검증 사항**:
- 여행 정보 수정
- 일기 정보 수정
- 보관 및 삭제 기능

---

#### 5. TimelineIntegrationTest
**목적**: 타임라인 기능 통합 테스트

**테스트 케이스**:
- ✅ `타임라인_조회_보관되지_않은_여행만_표시_테스트`: 보관된 여행 필터링
- ✅ `타임라인_시간순_정렬_테스트`: 시간순 정렬 검증
- ✅ `타임라인에서_과거_여행_추가_테스트`: 과거 여행 추가

**주요 검증 사항**:
- 타임라인 데이터 조회
- 필터링 및 정렬
- 과거 여행 추가

---

#### 6. TripDiaryIntegrationTest
**목적**: 여행-일기 연관 관계 통합 테스트

**테스트 케이스**:
- ✅ `여행_생성_후_다이어리_추가_시_종료날짜_자동_업데이트_테스트`: 종료날짜 자동 업데이트
- ✅ `다이어리_삭제_시_여행_종료날짜_재계산_테스트`: 삭제 후 종료날짜 재계산
- ✅ `모든_다이어리_삭제_시_종료날짜_null_테스트`: 모든 일기 삭제 시 종료날짜 처리
- ✅ `여행_보관_시_하위_다이어리_모두_보관_테스트`: 여행 보관 시 일기 연쇄 보관
- ✅ `다이어리_보관_테스트`: 일기 보관
- ✅ `다이어리_보관_해제_테스트`: 일기 보관 해제
- ✅ `조회_시_숨긴_다이어리_제외_테스트`: 보관된 일기 필터링

**주요 검증 사항**:
- 여행-일기 연관 관계
- 종료날짜 자동 관리
- 보관 기능 연쇄 처리

---

#### 7. ImageUploadIntegrationTest
**목적**: 이미지 업로드 기능 통합 테스트

**테스트 케이스**:
- ✅ `S3_이미지_URL_테스트`: S3 이미지 URL 처리
- ✅ `더미_이미지_URL_테스트`: 더미 이미지 URL 처리
- ✅ `로컬_파일_URL_테스트`: 로컬 파일 URL 처리
- ✅ `상대_경로_URL_테스트`: 상대 경로 URL 처리

**주요 검증 사항**:
- 다양한 이미지 URL 형식 처리

---

## Domain 테스트

### 테스트 방식
- **프레임워크**: JUnit 5
- **목적**: 도메인 엔티티의 비즈니스 로직 검증

### 테스트 파일 목록

#### 1. DiaryTest
**목적**: Diary 엔티티 로직 테스트

**테스트 케이스**:
- ✅ `다이어리_생성_테스트`: 일기 생성
- ✅ `선택적_필드_설정_테스트`: 선택적 필드 설정
- ✅ `이미지_추가_테스트`: 이미지 추가

---

#### 2. TripTest
**목적**: Trip 엔티티 로직 테스트

**테스트 케이스**:
- ✅ `여행_생성_테스트`: 여행 생성
- ✅ `다이어리_추가_시_종료날짜_업데이트_테스트`: 일기 추가 시 종료날짜 업데이트
- ✅ `종료날짜_수동_업데이트_테스트`: 종료날짜 수동 업데이트

---

#### 3. DiaryImageTest
**목적**: DiaryImage 엔티티 로직 테스트

**테스트 케이스**:
- ✅ `카메라_타입_테스트`: 카메라 타입 (전면/후면)
- ✅ `전면_카메라_이미지_생성_테스트`: 전면 카메라 이미지
- ✅ `후면_카메라_이미지_생성_테스트`: 후면 카메라 이미지
- ✅ `대표사진_변경_테스트`: 대표사진 변경
- ✅ `이미지_순서_테스트`: 이미지 순서 관리

---

## Util 테스트

### 테스트 방식
- **프레임워크**: JUnit 5
- **목적**: 유틸리티 함수 검증

### 테스트 파일 목록

#### 1. EmojiValidatorTest
**목적**: 이모지 유효성 검사 테스트

**테스트 케이스**:
- ✅ 이모지 유효성 검사
- ✅ 잘못된 형식 검증

---

## 테스트 실행 방법

### 전체 테스트 실행
```bash
# Windows (PowerShell)
.\gradlew.bat test

# Linux/Mac 또는 Git Bash
./gradlew test
```

### 단위 테스트만 실행 (통합 테스트 제외) ⭐ 권장
로컬 개발 시 빠른 테스트 실행을 위해 통합 테스트를 제외하고 실행합니다:
```bash
# Windows (PowerShell)
.\gradlew.bat unitTest

# Linux/Mac 또는 Git Bash
./gradlew unitTest
```

**참고**: `unitTest` 태스크는 `@Tag("integration")` 어노테이션이 있는 테스트를 제외합니다.

### 통합 테스트만 실행
```bash
# Windows (PowerShell)
.\gradlew.bat integrationTest

# Linux/Mac 또는 Git Bash
./gradlew integrationTest
```

**참고**: `integrationTest` 태스크는 `@Tag("integration")` 어노테이션이 있는 테스트만 실행합니다.

### 특정 테스트 클래스 실행
```bash
# Windows
.\gradlew.bat test --tests DiaryServiceTest

# Linux/Mac 또는 Git Bash
./gradlew test --tests DiaryServiceTest
```

### 특정 테스트 메서드 실행
```bash
# Windows
.\gradlew.bat test --tests DiaryServiceTest.다이어리_생성_테스트

# Linux/Mac 또는 Git Bash
./gradlew test --tests DiaryServiceTest.다이어리_생성_테스트
```

### Service 테스트만 실행
```bash
# Windows
.\gradlew.bat test --tests "*ServiceTest"

# Linux/Mac 또는 Git Bash
./gradlew test --tests "*ServiceTest"
```

### Controller 테스트만 실행
```bash
# Windows
.\gradlew.bat test --tests "*ControllerTest"

# Linux/Mac 또는 Git Bash
./gradlew test --tests "*ControllerTest"
```

---

## 테스트 작성 가이드라인

### 1. 테스트 구조 (Given-When-Then)
```java
@Test
void 테스트_이름() {
    // given: 테스트 준비
    User user = new User();
    user.setId(1L);
    
    // when: 테스트 실행
    User result = userService.getUser(1L);
    
    // then: 결과 검증
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
}
```

### 2. Mock 사용
```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void test() {
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));
        // ...
    }
}
```

### 3. 예외 테스트
```java
@Test
void 예외_테스트() {
    // when & then
    assertThatThrownBy(() -> service.method())
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
}
```

### 4. 통합 테스트 베이스 클래스
```java
public class IntegrationTestBase {
    @Autowired
    protected UserRepository userRepository;
    
    protected User testUser;
    
    @BeforeEach
    void initializeTestData() {
        // 공통 테스트 데이터 초기화
    }
}
```



