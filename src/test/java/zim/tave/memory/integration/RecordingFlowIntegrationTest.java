package zim.tave.memory.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.request.CreateDiaryRequest;
import zim.tave.memory.dto.request.CreateTripRequest;
import zim.tave.memory.dto.response.DiaryResponseDto;
import zim.tave.memory.dto.response.TripResponseDto;
import zim.tave.memory.service.DiaryService;
import zim.tave.memory.service.TripService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 기록하기 플로우 통합 테스트
 * - 새 여행 생성 후 일기 추가
 * - 기존 여행에 일기 추가
 * - 사진 업로드 및 대표사진 선택
 * - 감정색/날씨 선택
 */
public class RecordingFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TripService tripService;

    @Autowired
    private DiaryService diaryService;

    @BeforeEach
    void setUp() {
        initializeTestData();
    }

    @Test
    void 새_여행_생성_후_일기_추가_테스트() {
        // given - 새 여행 생성
        CreateTripRequest tripRequest = new CreateTripRequest();
        tripRequest.setTripName("제주도 여행");
        tripRequest.setDescription("제주도 3박 4일 여행");
        tripRequest.setThemeId(defaultTheme.getId());
        tripRequest.setStartDate(LocalDate.of(2024, 1, 1));
        tripRequest.setEndDate(LocalDate.of(2024, 1, 4));

        TripResponseDto createdTrip = tripService.createTrip(tripRequest, testUser.getId());

        // when - 일기 추가 (사진 2장, 대표사진 선택, 감정색/날씨 선택)
        CreateDiaryRequest diaryRequest = new CreateDiaryRequest();
        diaryRequest.setUserId(testUser.getId());
        diaryRequest.setTripId(createdTrip.getId());
        diaryRequest.setCountryCode("KR");
        diaryRequest.setCity("제주시");
        // dateTime 필드 제거 - 서버에서 UTC 기준으로 자동 생성됨
        diaryRequest.setContent("제주도 첫째 날 - 한라산 등반");
        diaryRequest.setDetailedLocation("한라산 정상");
        diaryRequest.setEmotionId(defaultEmotion.getId());
        diaryRequest.setWeatherId(sunnyWeather.getId());

        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front1.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back1.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(false);

        diaryRequest.setImages(Arrays.asList(frontImage, backImage));

        DiaryResponseDto createdDiary = diaryService.createDiary(diaryRequest);

        // then
        assertThat(createdTrip).isNotNull();
        assertThat(createdTrip.getTripName()).isEqualTo("제주도 여행");
        assertThat(createdDiary).isNotNull();
        assertThat(createdDiary.getCity()).isEqualTo("제주시");
        assertThat(createdDiary.getContent()).isEqualTo("제주도 첫째 날 - 한라산 등반");

        // 일기 이미지 확인
        Diary savedDiary = diaryRepository.findById(createdDiary.getId()).orElseThrow();
        assertThat(savedDiary.getDiaryImages()).hasSize(2);
        assertThat(savedDiary.getDiaryImages()).anyMatch(img -> 
            img.getCameraType() == DiaryImage.CameraType.FRONT && img.isRepresentative());
        assertThat(savedDiary.getDiaryImages()).anyMatch(img -> 
            img.getCameraType() == DiaryImage.CameraType.BACK && !img.isRepresentative());

        // 감정/날씨 확인
        assertThat(savedDiary.getEmotion()).isNotNull();
        assertThat(savedDiary.getWeather()).isNotNull();

        // 여행 종료 날짜가 일기 날짜로 업데이트되었는지 확인
        Trip trip = tripRepository.findById(createdTrip.getId()).orElseThrow();
        assertThat(trip.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    void 기존_여행에_일기_추가_테스트() {
        // given - 기존 여행 생성
        Trip existingTrip = createTestTrip(
            "제주도 여행",
            "제주도 3박 4일 여행",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 4)
        );

        // 첫 번째 일기 추가
        CreateDiaryRequest firstDiaryRequest = new CreateDiaryRequest();
        firstDiaryRequest.setUserId(testUser.getId());
        firstDiaryRequest.setTripId(existingTrip.getId());
        firstDiaryRequest.setCountryCode("KR");
        firstDiaryRequest.setCity("제주시");
        // dateTime 필드 제거 - 서버에서 UTC 기준으로 자동 생성됨
        firstDiaryRequest.setContent("첫째 날 일기");
        firstDiaryRequest.setEmotionId(defaultEmotion.getId());
        firstDiaryRequest.setWeatherId(sunnyWeather.getId());

        CreateDiaryRequest.DiaryImageInfo frontImage1 = new CreateDiaryRequest.DiaryImageInfo();
        frontImage1.setImageUrl("https://example.com/front1.jpg");
        frontImage1.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage1.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage1 = new CreateDiaryRequest.DiaryImageInfo();
        backImage1.setImageUrl("https://example.com/back1.jpg");
        backImage1.setCameraType(DiaryImage.CameraType.BACK);
        backImage1.setRepresentative(false);

        firstDiaryRequest.setImages(Arrays.asList(frontImage1, backImage1));
        DiaryResponseDto firstDiary = diaryService.createDiary(firstDiaryRequest);

        // when - 두 번째 일기 추가
        CreateDiaryRequest secondDiaryRequest = new CreateDiaryRequest();
        secondDiaryRequest.setUserId(testUser.getId());
        secondDiaryRequest.setTripId(existingTrip.getId());
        secondDiaryRequest.setCountryCode("KR");
        secondDiaryRequest.setCity("서귀포시");
        // dateTime 필드 제거 - 서버에서 UTC 기준으로 자동 생성됨
        secondDiaryRequest.setContent("둘째 날 일기");
        secondDiaryRequest.setEmotionId(defaultEmotion.getId());
        secondDiaryRequest.setWeatherId(sunnyWeather.getId());

        CreateDiaryRequest.DiaryImageInfo frontImage2 = new CreateDiaryRequest.DiaryImageInfo();
        frontImage2.setImageUrl("https://example.com/front2.jpg");
        frontImage2.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage2.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage2 = new CreateDiaryRequest.DiaryImageInfo();
        backImage2.setImageUrl("https://example.com/back2.jpg");
        backImage2.setCameraType(DiaryImage.CameraType.BACK);
        backImage2.setRepresentative(false);

        secondDiaryRequest.setImages(Arrays.asList(frontImage2, backImage2));
        DiaryResponseDto secondDiary = diaryService.createDiary(secondDiaryRequest);

        // then
        assertThat(firstDiary).isNotNull();
        assertThat(secondDiary).isNotNull();
        assertThat(secondDiary.getCity()).isEqualTo("서귀포시");

        // 여행에 두 개의 일기가 있는지 확인
        Trip trip = tripRepository.findById(existingTrip.getId()).orElseThrow();
        List<Diary> diaries = diaryRepository.findByTrip_Id(trip.getId());
        assertThat(diaries).hasSize(2);

        // 여행 종료 날짜가 마지막 일기 날짜로 업데이트되었는지 확인
        assertThat(trip.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 2));
    }

    @Test
    void 대표사진_선택_테스트() {
        // given - 여행 생성
        Trip trip = createTestTrip(
            "제주도 여행",
            "제주도 여행",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 4)
        );

        // when - 일기 생성 시 BACK 이미지를 대표사진으로 선택
        CreateDiaryRequest diaryRequest = new CreateDiaryRequest();
        diaryRequest.setUserId(testUser.getId());
        diaryRequest.setTripId(trip.getId());
        diaryRequest.setCountryCode("KR");
        diaryRequest.setCity("제주시");
        // dateTime 필드 제거 - 서버에서 UTC 기준으로 자동 생성됨
        diaryRequest.setContent("대표사진 테스트");
        diaryRequest.setEmotionId(defaultEmotion.getId());

        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(false);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(true); // BACK을 대표사진으로 선택

        diaryRequest.setImages(Arrays.asList(frontImage, backImage));
        DiaryResponseDto createdDiary = diaryService.createDiary(diaryRequest);

        // then - BACK 이미지가 대표사진인지 확인
        Diary savedDiary = diaryRepository.findById(createdDiary.getId()).orElseThrow();
        DiaryImage representativeImage = savedDiary.getDiaryImages().stream()
            .filter(DiaryImage::isRepresentative)
            .findFirst()
            .orElseThrow();

        assertThat(representativeImage.getCameraType()).isEqualTo(DiaryImage.CameraType.BACK);
        assertThat(representativeImage.getImageUrl()).isEqualTo("https://example.com/back.jpg");
    }

    @Test
    void 감정색_기본값_테스트() {
        // given - 여행 생성
        Trip trip = createTestTrip(
            "제주도 여행",
            "제주도 여행",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 4)
        );

        // when - 감정색을 선택하지 않고 일기 생성
        CreateDiaryRequest diaryRequest = new CreateDiaryRequest();
        diaryRequest.setUserId(testUser.getId());
        diaryRequest.setTripId(trip.getId());
        diaryRequest.setCountryCode("KR");
        diaryRequest.setCity("제주시");
        // dateTime 필드 제거 - 서버에서 UTC 기준으로 자동 생성됨
        diaryRequest.setContent("감정색 기본값 테스트");
        // emotionId를 설정하지 않음 (기본값 사용)

        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(false);

        diaryRequest.setImages(Arrays.asList(frontImage, backImage));
        DiaryResponseDto createdDiary = diaryService.createDiary(diaryRequest);

        // then - 기본 감정색이 적용되었는지 확인
        Diary savedDiary = diaryRepository.findById(createdDiary.getId()).orElseThrow();
        assertThat(savedDiary.getEmotion()).isNotNull();
        assertThat(savedDiary.getEmotion().getName()).isEqualTo("기본");
        assertThat(savedDiary.getEmotion().getColorCode()).isEqualTo("#EEEEEE");
    }

    @Test
    void 날씨_선택_안함_테스트() {
        // given - 여행 생성
        Trip trip = createTestTrip(
            "제주도 여행",
            "제주도 여행",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 4)
        );

        // when - 날씨를 선택하지 않고 일기 생성
        CreateDiaryRequest diaryRequest = new CreateDiaryRequest();
        diaryRequest.setUserId(testUser.getId());
        diaryRequest.setTripId(trip.getId());
        diaryRequest.setCountryCode("KR");
        diaryRequest.setCity("제주시");
        // dateTime 필드 제거 - 서버에서 UTC 기준으로 자동 생성됨
        diaryRequest.setContent("날씨 선택 안함 테스트");
        diaryRequest.setEmotionId(defaultEmotion.getId());
        // weatherId를 설정하지 않음

        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(false);

        diaryRequest.setImages(Arrays.asList(frontImage, backImage));
        DiaryResponseDto createdDiary = diaryService.createDiary(diaryRequest);

        // then - 날씨가 null인지 확인
        Diary savedDiary = diaryRepository.findById(createdDiary.getId()).orElseThrow();
        assertThat(savedDiary.getWeather()).isNull();
    }
}

