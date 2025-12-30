package zim.tave.memory.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.request.UpdateDiaryOptionalFieldsRequest;
import zim.tave.memory.dto.request.UpdateTripRequest;
import zim.tave.memory.service.DiaryService;
import zim.tave.memory.service.TripService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 전체 기록 보기 플로우 통합 테스트
 * - 여행 수정 (이름, 대표 이미지, 테마, 날짜)
 * - 여행 보관/삭제
 * - 일기 수정 (장소명, 기록, 감정색 기본값, 날씨 기본값)
 */
public class ViewRecordsFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TripService tripService;

    @Autowired
    private DiaryService diaryService;

    private Trip testTrip;
    private Diary testDiary;

    @BeforeEach
    void setUp() {
        initializeTestData();

        // 테스트용 여행 생성
        testTrip = createTestTrip(
            "제주도 여행",
            "제주도 3박 4일",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 4)
        );

        // 테스트용 일기 생성
        testDiary = createTestDiary(
            testTrip,
            "제주시",
            LocalDateTime.of(2024, 1, 1, 10, 0),
            "제주도 첫째 날"
        );

        // 일기 이미지 추가
        createTestDiaryImage(testDiary, "https://example.com/front.jpg",
            DiaryImage.CameraType.FRONT, true, 1);
        createTestDiaryImage(testDiary, "https://example.com/back.jpg",
            DiaryImage.CameraType.BACK, false, 2);
    }

    @Test
    void 여행_이름_수정_테스트() {
        // given
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 제주도 여행");
        request.setStartDate(testTrip.getStartDate());
        request.setEndDate(testTrip.getEndDate());

        // when
        tripService.updateTrip(testTrip.getId(), request, testUser.getId());

        // then
        Trip updatedTrip = tripRepository.findById(testTrip.getId()).orElseThrow();
        assertThat(updatedTrip.getTripName()).isEqualTo("수정된 제주도 여행");
    }

    @Test
    void 여행_대표_이미지_수정_테스트() {
        // given
        String newRepresentativeImageUrl = "https://example.com/new-representative.jpg";
        UpdateTripRequest request = new UpdateTripRequest();
        request.setRepresentativeImageUrl(newRepresentativeImageUrl);
        request.setStartDate(testTrip.getStartDate());
        request.setEndDate(testTrip.getEndDate());

        // when
        tripService.updateTrip(testTrip.getId(), request, testUser.getId());

        // then
        Trip updatedTrip = tripRepository.findById(testTrip.getId()).orElseThrow();
        assertThat(updatedTrip.getRepresentativeImageUrl()).isEqualTo(newRepresentativeImageUrl);
    }

    @Test
    void 여행_테마_수정_테스트() {
        // given - 새로운 테마 생성
        TripTheme newTheme = new TripTheme("새 테마",
            "https://example.com/new_theme_sample.png",
            "https://example.com/new_theme_card.png");
        tripThemeRepository.save(newTheme);

        UpdateTripRequest request = new UpdateTripRequest();
        request.setThemeId(newTheme.getId());
        request.setStartDate(testTrip.getStartDate());
        request.setEndDate(testTrip.getEndDate());

        // when
        tripService.updateTrip(testTrip.getId(), request, testUser.getId());

        // then
        Trip updatedTrip = tripRepository.findById(testTrip.getId()).orElseThrow();
        assertThat(updatedTrip.getTripTheme().getId()).isEqualTo(newTheme.getId());
    }

    @Test
    void 여행_날짜_수정_테스트() {
        // given
        LocalDate newStartDate = LocalDate.of(2024, 1, 5);
        LocalDate newEndDate = LocalDate.of(2024, 1, 8);

        UpdateTripRequest request = new UpdateTripRequest();
        request.setStartDate(newStartDate);
        request.setEndDate(newEndDate);

        // when
        tripService.updateTrip(testTrip.getId(), request, testUser.getId());

        // then
        Trip updatedTrip = tripRepository.findById(testTrip.getId()).orElseThrow();
        assertThat(updatedTrip.getStartDate()).isEqualTo(newStartDate);
        assertThat(updatedTrip.getEndDate()).isEqualTo(newEndDate);
    }

    @Test
    void 여행_보관_테스트() {
        // when
        tripService.storeTrip(testTrip.getId(), testUser.getId(), true);

        // then
        Trip storedTrip = tripRepository.findById(testTrip.getId()).orElseThrow();
        assertThat(storedTrip.getIsStored()).isTrue();

        // 일기도 함께 보관되었는지 확인
        List<Diary> diaries = diaryRepository.findByTripId(testTrip.getId());
        diaries.forEach(diary -> assertThat(diary.getIsStored()).isTrue());
    }

    @Test
    void 여행_삭제_테스트() {
        // when
        tripService.deleteTrip(testTrip.getId(), testUser.getId());

        // then
        assertThat(tripRepository.findById(testTrip.getId())).isEmpty();
    }

    @Test
    void 일기_장소명_수정_테스트() {
        // given
        UpdateDiaryOptionalFieldsRequest request = new UpdateDiaryOptionalFieldsRequest();
        request.setCity("서귀포시");

        // when
        diaryService.updateDiaryOptionalFields(testUser.getId(), testDiary.getId(), request);

        // then
        Diary updatedDiary = diaryRepository.findById(testDiary.getId()).orElseThrow();
        assertThat(updatedDiary.getCity()).isEqualTo("서귀포시");
    }

    @Test
    void 일기_기록_수정_테스트() {
        // given
        UpdateDiaryOptionalFieldsRequest request = new UpdateDiaryOptionalFieldsRequest();
        request.setContent("수정된 일기 내용입니다.");

        // when
        diaryService.updateDiaryOptionalFields(testUser.getId(), testDiary.getId(), request);

        // then
        Diary updatedDiary = diaryRepository.findById(testDiary.getId()).orElseThrow();
        assertThat(updatedDiary.getContent()).isEqualTo("수정된 일기 내용입니다.");
    }

    @Test
    void 일기_감정색_수정_기본값_테스트() {
        // given - 감정색이 없는 일기
        testDiary.setEmotion(null);
        diaryRepository.save(testDiary);

        // 새로운 감정 생성
        Emotion newEmotion = new Emotion("즐거움", "#FFE13E");
        emotionRepository.save(newEmotion);

        UpdateDiaryOptionalFieldsRequest request = new UpdateDiaryOptionalFieldsRequest();
        request.setEmotionId(newEmotion.getId());

        // when
        diaryService.updateDiaryOptionalFields(testUser.getId(), testDiary.getId(), request);

        // then
        Diary updatedDiary = diaryRepository.findById(testDiary.getId()).orElseThrow();
        assertThat(updatedDiary.getEmotion()).isNotNull();
        assertThat(updatedDiary.getEmotion().getName()).isEqualTo("즐거움");
    }

    @Test
    void 일기_날씨_수정_기본값_테스트() {
        // given - 날씨가 없는 일기
        testDiary.setWeather(null);
        diaryRepository.save(testDiary);

        // 날씨를 선택하지 않으면 null 유지 (기본값 = 선택 안 함)
        UpdateDiaryOptionalFieldsRequest request = new UpdateDiaryOptionalFieldsRequest();
        // weatherId를 설정하지 않음

        // when
        diaryService.updateDiaryOptionalFields(testUser.getId(), testDiary.getId(), request);

        // then
        Diary updatedDiary = diaryRepository.findById(testDiary.getId()).orElseThrow();
        assertThat(updatedDiary.getWeather()).isNull();
    }
}

