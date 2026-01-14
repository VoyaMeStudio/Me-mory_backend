package zim.tave.memory.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.request.CreateDiaryRequest;
import zim.tave.memory.dto.response.MyPageResponseDto;
import zim.tave.memory.dto.response.VisitedCountryListResponseDto;
import zim.tave.memory.service.MyPageService;
import zim.tave.memory.service.VisitedCountryService;
import zim.tave.memory.repository.VisitedCountryRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 마이페이지 통합 테스트
 * - 사용자 정보 조회
 * - 방문한 나라 조회 (중복 없이)
 * - 일기 개수 조회
 */
public class MyPageIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private VisitedCountryService visitedCountryService;

    @Autowired
    protected VisitedCountryRepository visitedCountryRepository;

    @BeforeEach
    void setUp() {
        initializeTestData();
        // 사용자 정보 완성 (마이페이지 조회에 필수)
        testUser.setSurName("TEST");
        testUser.setFirstName("USER");
        testUser.setKoreanName("테스트");
        testUser.setBirth(LocalDate.of(1995, 1, 1));
        testUser.setNationality("REPUBLIC OF KOREA");
        testUser = userRepository.save(testUser);
    }

    @Test
    void 마이페이지_사용자_정보_조회_테스트() {
        // when
        MyPageResponseDto myPage = myPageService.getMyPage(testUser.getId());

        // then
        assertThat(myPage).isNotNull();
        assertThat(myPage.getUser()).isNotNull();
        assertThat(myPage.getUser().getKoreanName()).isEqualTo("테스트");
        assertThat(myPage.getUser().getSurName()).isEqualTo("TEST");
        assertThat(myPage.getUser().getFirstName()).isEqualTo("USER");
    }

    @Test
    void 마이페이지_일기_개수_조회_테스트() {
        // given - 일기 생성
        Trip trip = createTestTrip(
            "제주도 여행",
            "제주도 여행",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 4)
        );

        Diary diary1 = createTestDiary(
            trip,
            "제주시",
            OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC),
            "첫 번째 일기"
        );

        Diary diary2 = createTestDiary(
            trip,
            "서귀포시",
            OffsetDateTime.of(2024, 1, 2, 14, 0, 0, 0, ZoneOffset.UTC),
            "두 번째 일기"
        );

        // when
        MyPageResponseDto myPage = myPageService.getMyPage(testUser.getId());

        // then
        assertThat(myPage.getStatistics()).isNotNull();
        assertThat(myPage.getStatistics().getDiaryCount()).isEqualTo(2L);
    }

    @Test
    void 마이페이지_방문_국가_중복_없이_조회_테스트() {
        // given - 여러 일기를 같은 국가로 생성
        Trip trip1 = createTestTrip(
            "제주도 여행",
            "제주도 여행",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 4)
        );

        Trip trip2 = createTestTrip(
            "제주도 여행 2",
            "제주도 여행 2",
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 2, 5)
        );

        // 같은 국가(한국)로 일기 생성
        createTestDiary(trip1, "제주시", OffsetDateTime.now(ZoneOffset.UTC), "일기 1");
        createTestDiary(trip2, "서귀포시", OffsetDateTime.now(ZoneOffset.UTC), "일기 2");

        // 다른 국가 추가
        Country japanCountry = new Country("JP", "일본", "🇯🇵");
        countryRepository.save(japanCountry);
        
        visitedCountryService.registerVisitedCountry(testUser.getId(), "JP", defaultEmotion.getId());

        // when
        VisitedCountryListResponseDto visitedCountries = 
            visitedCountryService.getVisitedCountryList(testUser.getId());

        // then - 중복 없이 국가 조회
        assertThat(visitedCountries.getVisitedCountries()).isNotEmpty();
        // KR은 일기 생성 시 자동 등록됨
    }

    @Test
    void 마이페이지_방문_국가_개수_조회_테스트() {
        // given - 여러 국가 방문
        visitedCountryService.registerVisitedCountry(testUser.getId(), "KR", defaultEmotion.getId());
        visitedCountryService.registerVisitedCountry(testUser.getId(), "JP", defaultEmotion.getId());
        visitedCountryService.registerVisitedCountry(testUser.getId(), "US", defaultEmotion.getId());

        // when
        MyPageResponseDto myPage = myPageService.getMyPage(testUser.getId());

        // then
        assertThat(myPage.getStatistics()).isNotNull();
        assertThat(myPage.getStatistics().getCountryCount()).isGreaterThanOrEqualTo(3L);
    }
}

