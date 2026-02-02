package zim.tave.memory.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.*;
import zim.tave.memory.integration.AbstractContainerBaseTest;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * API 통합 테스트 베이스 클래스
 * - AbstractContainerBaseTest를 상속하여 공유 컨테이너 사용
 * - MockMvc를 사용한 HTTP 레벨 테스트
 * - JWT 토큰 생성 헬퍼 메서드 제공
 *
 * 주의: AbstractContainerBaseTest에서 @SpringBootTest(webEnvironment = NONE)를 설정했지만,
 * MockMvc를 사용하기 위해 @AutoConfigureMockMvc를 사용합니다.
 * 이는 Spring Context를 로드하지만, 컨테이너는 공유됩니다.
 */
@Tag("integration")
@AutoConfigureMockMvc
@Transactional
public abstract class ApiTestBase extends AbstractContainerBaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected TripThemeRepository tripThemeRepository;

    @Autowired
    protected EmotionRepository emotionRepository;

    @Autowired
    protected WeatherRepository weatherRepository;

    @Autowired
    protected CountryRepository countryRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TripRepository tripRepository;

    @Autowired
    protected DiaryRepository diaryRepository;

    @Autowired
    protected DiaryImageRepository diaryImageRepository;

    protected User testUser;
    protected TripTheme defaultTheme;
    protected Emotion defaultEmotion;
    protected Weather sunnyWeather;
    protected Country koreaCountry;

    /**
     * 테스트 데이터 초기화
     */
    protected void initializeTestData() {
        objectMapper.registerModule(new JavaTimeModule());
        initTripThemes();
        initEmotions();
        initWeathers();
        initCountries();
        initTestUser();
    }

    /**
     * JWT 토큰 생성 헬퍼 메서드
     */
    protected String generateToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return jwtUtil.generateAccessToken(user.getId(), user.getKakaoId());
    }

    /**
     * JWT 토큰을 Authorization 헤더에 추가하는 헤더 값 반환
     */
    protected String getAuthorizationHeader(Long userId) {
        return "Bearer " + generateToken(userId);
    }

    private void initTripThemes() {
        if (tripThemeRepository.count() == 0) {
            defaultTheme = new TripTheme("기본",
                    "https://example.com/default_sample.png",
                    "https://example.com/default_card.png");
            tripThemeRepository.save(defaultTheme);
        } else {
            defaultTheme = tripThemeRepository.findById(1L).orElse(null);
            if (defaultTheme == null) {
                defaultTheme = tripThemeRepository.findAll().get(0);
            }
        }
    }

    private void initEmotions() {
        if (emotionRepository.count() == 0) {
            defaultEmotion = new Emotion("기본", "#EEEEEE");
            emotionRepository.save(defaultEmotion);

            emotionRepository.save(new Emotion("설렘", "#FDD7DE"));
            emotionRepository.save(new Emotion("신기함", "#FFCB6B"));
            emotionRepository.save(new Emotion("즐거움", "#FFE13E"));
            emotionRepository.save(new Emotion("힐링", "#C1E8A0"));
        } else {
            defaultEmotion = emotionRepository.findById(1L).orElse(null);
            if (defaultEmotion == null) {
                defaultEmotion = emotionRepository.findAll().get(0);
            }
        }
    }

    private void initWeathers() {
        if (weatherRepository.count() == 0) {
            sunnyWeather = new Weather();
            sunnyWeather.setName("맑음");
            sunnyWeather.setIconUrl("https://example.com/sunny.jpg");
            weatherRepository.save(sunnyWeather);

            Weather cloudy = new Weather();
            cloudy.setName("구름");
            cloudy.setIconUrl("https://example.com/cloudy.jpg");
            weatherRepository.save(cloudy);

            Weather rainy = new Weather();
            rainy.setName("비");
            rainy.setIconUrl("https://example.com/rainy.jpg");
            weatherRepository.save(rainy);
        } else {
            sunnyWeather = weatherRepository.findById(1L).orElse(null);
            if (sunnyWeather == null) {
                sunnyWeather = weatherRepository.findAll().get(0);
            }
        }
    }

    private void initCountries() {
        if (countryRepository.count() == 0) {
            koreaCountry = new Country("KR", "대한민국", "🇰🇷");
            countryRepository.save(koreaCountry);

            countryRepository.save(new Country("US", "미국", "🇺🇸"));
            countryRepository.save(new Country("JP", "일본", "🇯🇵"));
            countryRepository.save(new Country("CN", "중국", "🇨🇳"));
        } else {
            koreaCountry = countryRepository.findById("KR").orElse(null);
            if (koreaCountry == null) {
                koreaCountry = countryRepository.findAll().get(0);
            }
        }
    }

    private void initTestUser() {
        if (userRepository.findByKakaoId("test_user").isEmpty()) {
            testUser = new User();
            testUser.setKakaoId("test_user");
            testUser.setSurName("TEST");
            testUser.setFirstName("USER");
            testUser.setKoreanName("테스트");
            testUser.setBirth(LocalDate.of(1995, 1, 1));
            testUser.setNationality("REPUBLIC OF KOREA");
            testUser.setCreatedAt(LocalDate.now());
            testUser.setStatus(true);
            testUser.setRegistered(true);
            testUser.setDiaryCount(0L);
            testUser.setVisitedCountryCount(0L);
            testUser.setFlags("🇰🇷");
            testUser.setProfileImageUrl("https://example.com/profile.jpg");
            testUser = userRepository.save(testUser);
        } else {
            testUser = userRepository.findByKakaoId("test_user").orElseThrow();
        }
    }

    /**
     * 테스트용 여행 생성 헬퍼 메서드
     */
    protected Trip createTestTrip(String tripName, String description, LocalDate startDate, LocalDate endDate) {
        Trip trip = Trip.createTrip(testUser, tripName, description, defaultTheme);
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setIsStored(false);
        trip.setIsPast(false);
        return tripRepository.save(trip);
    }

    /**
     * 테스트용 일기 생성 헬퍼 메서드
     */
    protected Diary createTestDiary(Trip trip, String city, OffsetDateTime dateTime, String content) {
        Diary diary = Diary.createDiary(testUser, trip, koreaCountry, city, dateTime, content);
        diary.setOptionalFields(null, defaultEmotion, sunnyWeather);
        return diaryRepository.save(diary);
    }

    /**
     * 테스트용 일기 이미지 생성 헬퍼 메서드
     */
    protected DiaryImage createTestDiaryImage(Diary diary, String imageUrl, DiaryImage.CameraType cameraType,
                                               boolean isRepresentative, int imageOrder) {
        DiaryImage image = new DiaryImage();
        image.setDiary(diary);
        image.setImageUrl(imageUrl);
        image.setCameraType(cameraType);
        image.setRepresentative(isRepresentative);
        image.setImageOrder(imageOrder);
        diary.addDiaryImage(image);
        return diaryImageRepository.save(image);
    }
}

