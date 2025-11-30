package zim.tave.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.Diary;
import zim.tave.memory.domain.DiaryImage;
import zim.tave.memory.domain.Emotion;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.TimelineTripDto;
import zim.tave.memory.dto.response.TimelineResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.EmotionRepository;
import zim.tave.memory.repository.TripRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimelineServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private EmotionRepository emotionRepository;

    @InjectMocks
    private TimelineService timelineService;

    private User user;
    private Trip activeTrip;
    private Trip storedTrip;
    private Diary activeDiary;
    private Diary storedDiary;
    private Country country;
    private Emotion emotion;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        country = new Country("KR", "대한민국", "🇰🇷");
        
        emotion = new Emotion();
        emotion.setId(1L);
        emotion.setName("설렘");
        emotion.setColorCode("#FF6B6B");

        // 활성 여행 (isStored = false)
        activeTrip = new Trip();
        activeTrip.setId(1L);
        activeTrip.setTripName("제주도 여행");
        activeTrip.setDescription("제주도 3박 4일 여행");
        activeTrip.setUser(user);
        activeTrip.setStartDate(LocalDate.of(2024, 1, 1));
        activeTrip.setEndDate(LocalDate.of(2024, 1, 5));
        activeTrip.setIsStored(false);
        activeTrip.setIsPast(false);
        activeTrip.setDiaries(new ArrayList<>());

        // 보관된 여행 (isStored = true)
        storedTrip = new Trip();
        storedTrip.setId(2L);
        storedTrip.setTripName("보관된 여행");
        storedTrip.setDescription("보관된 여행 설명");
        storedTrip.setUser(user);
        storedTrip.setStartDate(LocalDate.of(2024, 2, 1));
        storedTrip.setEndDate(LocalDate.of(2024, 2, 5));
        storedTrip.setIsStored(true);
        storedTrip.setIsPast(false);
        storedTrip.setDiaries(new ArrayList<>());

        // 활성 일기 (isStored = false)
        activeDiary = new Diary();
        activeDiary.setId(1L);
        activeDiary.setUser(user);
        activeDiary.setTrip(activeTrip);
        activeDiary.setCountry(country);
        activeDiary.setCity("제주시");
        activeDiary.setDateTime(LocalDateTime.of(2024, 1, 2, 10, 0));
        activeDiary.setContent("제주도 일기 내용");
        activeDiary.setIsStored(false);
        activeDiary.setCreatedAt(LocalDateTime.of(2024, 1, 2, 10, 0));
        activeDiary.setEmotion(emotion);
        activeDiary.setDiaryImages(new ArrayList<>());

        // 보관된 일기 (isStored = true)
        storedDiary = new Diary();
        storedDiary.setId(2L);
        storedDiary.setUser(user);
        storedDiary.setTrip(activeTrip);
        storedDiary.setCountry(country);
        storedDiary.setCity("서귀포시");
        storedDiary.setDateTime(LocalDateTime.of(2024, 1, 3, 10, 0));
        storedDiary.setContent("보관된 일기 내용");
        storedDiary.setIsStored(true);
        storedDiary.setCreatedAt(LocalDateTime.of(2024, 1, 3, 10, 0));
        storedDiary.setDiaryImages(new ArrayList<>());
    }

    @Test
    void 타임라인_조회_활성_여행만_포함() {
        // given
        Emotion defaultEmotion = new Emotion();
        defaultEmotion.setId(1L);
        defaultEmotion.setName("기본");
        defaultEmotion.setColorCode("#EEEEEE");

        List<Trip> trips = Arrays.asList(activeTrip, storedTrip);

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);
        when(emotionRepository.findByName("기본")).thenReturn(defaultEmotion);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).hasSize(1);
        assertThat(result.getTrips().get(0).getTripId()).isEqualTo(1L);
        assertThat(result.getTrips().get(0).getTripName()).isEqualTo("제주도 여행");
        verify(tripRepository).findActiveTripsWithDetails(1L);
    }

    @Test
    void 타임라인_조회_보관된_여행_제외() {
        // given
        List<Trip> trips = Arrays.asList(storedTrip);

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).isEmpty();
    }

    @Test
    void 타임라인_조회_보관된_일기_제외() {
        // given
        activeTrip.setDiaries(Arrays.asList(activeDiary, storedDiary));
        List<Trip> trips = Arrays.asList(activeTrip);

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).hasSize(1);
        // 활성 일기만 포함되어야 하므로 방문 국가는 1개만 있어야 함
        assertThat(result.getTrips().get(0).getVisitedCountries()).hasSize(1);
        assertThat(result.getTrips().get(0).getVisitedCountries().get(0).getCountryCode()).isEqualTo("KR");
    }

    @Test
    void 타임라인_조회_인증_실패_null_userId() {
        // when & then
        assertThatThrownBy(() -> timelineService.getTimeline(null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    void 타임라인_조회_날짜_정렬_내림차순() {
        // given
        Trip trip1 = new Trip();
        trip1.setId(1L);
        trip1.setTripName("최근 여행");
        trip1.setUser(user);
        trip1.setStartDate(LocalDate.of(2024, 3, 1));
        trip1.setEndDate(LocalDate.of(2024, 3, 5));
        trip1.setIsStored(false);
        trip1.setDiaries(new ArrayList<>());

        Trip trip2 = new Trip();
        trip2.setId(2L);
        trip2.setTripName("과거 여행");
        trip2.setUser(user);
        trip2.setStartDate(LocalDate.of(2024, 1, 1));
        trip2.setEndDate(LocalDate.of(2024, 1, 5));
        trip2.setIsStored(false);
        trip2.setDiaries(new ArrayList<>());

        Emotion defaultEmotion = new Emotion();
        defaultEmotion.setId(1L);
        defaultEmotion.setName("기본");
        defaultEmotion.setColorCode("#EEEEEE");

        List<Trip> trips = Arrays.asList(trip2, trip1);

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);
        when(emotionRepository.findByName("기본")).thenReturn(defaultEmotion);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).hasSize(2);
        // 최근 날짜가 먼저 와야 함 (내림차순)
        assertThat(result.getTrips().get(0).getTripName()).isEqualTo("최근 여행");
        assertThat(result.getTrips().get(1).getTripName()).isEqualTo("과거 여행");
    }

    @Test
    void 타임라인_조회_방문_국가_수집() {
        // given
        Country country2 = new Country("US", "미국", "🇺🇸");
        Diary diary2 = new Diary();
        diary2.setId(3L);
        diary2.setUser(user);
        diary2.setTrip(activeTrip);
        diary2.setCountry(country2);
        diary2.setCity("뉴욕");
        diary2.setDateTime(LocalDateTime.of(2024, 1, 4, 10, 0));
        diary2.setContent("미국 일기");
        diary2.setIsStored(false);
        diary2.setCreatedAt(LocalDateTime.of(2024, 1, 4, 10, 0));
        diary2.setDiaryImages(new ArrayList<>());

        activeTrip.setDiaries(Arrays.asList(activeDiary, diary2));
        List<Trip> trips = Arrays.asList(activeTrip);

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).hasSize(1);
        // 두 개의 다른 국가가 있어야 함
        assertThat(result.getTrips().get(0).getVisitedCountries()).hasSize(2);
    }

    @Test
    void 타임라인_조회_대표_이미지_해결() {
        // given
        String representativeImageUrl = "https://example.com/image.jpg";
        activeTrip.setRepresentativeImageUrl(representativeImageUrl);
        List<Trip> trips = Arrays.asList(activeTrip);

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).hasSize(1);
        assertThat(result.getTrips().get(0).getRepresentativeImageUrl()).isEqualTo(representativeImageUrl);
    }

    @Test
    void 타임라인_조회_대표_이미지_없을때_일기에서_찾기() {
        // given
        DiaryImage diaryImage = new DiaryImage();
        diaryImage.setId(1L);
        diaryImage.setImageUrl("https://example.com/diary-image.jpg");
        diaryImage.setImageOrder(1);
        diaryImage.setRepresentative(true);
        diaryImage.setDiary(activeDiary);

        activeDiary.setDiaryImages(Arrays.asList(diaryImage));
        activeTrip.setDiaries(Arrays.asList(activeDiary));
        activeTrip.setRepresentativeImageUrl(null);
        List<Trip> trips = Arrays.asList(activeTrip);

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).hasSize(1);
        assertThat(result.getTrips().get(0).getRepresentativeImageUrl()).isEqualTo("https://example.com/diary-image.jpg");
    }

    @Test
    void 타임라인_조회_빈_여행_목록() {
        // given
        List<Trip> trips = new ArrayList<>();

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).isEmpty();
    }

    @Test
    void 타임라인_조회_일기_없는_여행() {
        // given
        Emotion defaultEmotion = new Emotion();
        defaultEmotion.setId(1L);
        defaultEmotion.setName("기본");
        defaultEmotion.setColorCode("#EEEEEE");

        activeTrip.setDiaries(new ArrayList<>());
        List<Trip> trips = Arrays.asList(activeTrip);

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);
        when(emotionRepository.findByName("기본")).thenReturn(defaultEmotion);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).hasSize(1);
        assertThat(result.getTrips().get(0).getVisitedCountries()).isEmpty();
        assertThat(result.getTrips().get(0).getRepresentativeImageUrl()).isNull();
        // 일기가 없으면 기본 감정 정보 반환
        assertThat(result.getTrips().get(0).getEmotionName()).isEqualTo("기본");
        assertThat(result.getTrips().get(0).getEmotionColor()).isEqualTo("#EEEEEE");
    }

    @Test
    void 타임라인_조회_여행별_감정색_가장_최근_일기에서_추출() {
        // given
        Emotion emotion2 = new Emotion();
        emotion2.setId(2L);
        emotion2.setName("행복");
        emotion2.setColorCode("#FFD700");

        Diary diary1 = new Diary();
        diary1.setId(1L);
        diary1.setUser(user);
        diary1.setTrip(activeTrip);
        diary1.setCountry(country);
        diary1.setCity("제주시");
        diary1.setDateTime(LocalDateTime.of(2024, 1, 2, 10, 0));
        diary1.setContent("첫 번째 일기");
        diary1.setIsStored(false);
        diary1.setCreatedAt(LocalDateTime.of(2024, 1, 2, 10, 0));
        diary1.setEmotion(emotion); // 설렘
        diary1.setDiaryImages(new ArrayList<>());

        Diary diary2 = new Diary();
        diary2.setId(2L);
        diary2.setUser(user);
        diary2.setTrip(activeTrip);
        diary2.setCountry(country);
        diary2.setCity("서귀포시");
        diary2.setDateTime(LocalDateTime.of(2024, 1, 3, 10, 0));
        diary2.setContent("두 번째 일기");
        diary2.setIsStored(false);
        diary2.setCreatedAt(LocalDateTime.of(2024, 1, 4, 10, 0)); // 더 최근
        diary2.setEmotion(emotion2); // 행복
        diary2.setDiaryImages(new ArrayList<>());

        activeTrip.setDiaries(Arrays.asList(diary1, diary2));
        List<Trip> trips = Arrays.asList(activeTrip);

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).hasSize(1);
        TimelineTripDto tripDto = result.getTrips().get(0);
        // 가장 최근 일기(diary2)의 감정 정보가 사용되어야 함
        assertThat(tripDto.getEmotionName()).isEqualTo("행복");
        assertThat(tripDto.getEmotionColor()).isEqualTo("#FFD700");
    }

    @Test
    void 타임라인_조회_감정정보_없는_일기() {
        // given
        Emotion defaultEmotion = new Emotion();
        defaultEmotion.setId(1L);
        defaultEmotion.setName("기본");
        defaultEmotion.setColorCode("#EEEEEE");

        Diary diaryWithoutEmotion = new Diary();
        diaryWithoutEmotion.setId(1L);
        diaryWithoutEmotion.setUser(user);
        diaryWithoutEmotion.setTrip(activeTrip);
        diaryWithoutEmotion.setCountry(country);
        diaryWithoutEmotion.setCity("제주시");
        diaryWithoutEmotion.setDateTime(LocalDateTime.of(2024, 1, 2, 10, 0));
        diaryWithoutEmotion.setContent("감정 없는 일기");
        diaryWithoutEmotion.setIsStored(false);
        diaryWithoutEmotion.setCreatedAt(LocalDateTime.of(2024, 1, 2, 10, 0));
        diaryWithoutEmotion.setEmotion(null);
        diaryWithoutEmotion.setDiaryImages(new ArrayList<>());

        activeTrip.setDiaries(Arrays.asList(diaryWithoutEmotion));
        List<Trip> trips = Arrays.asList(activeTrip);

        when(tripRepository.findActiveTripsWithDetails(1L)).thenReturn(trips);
        when(emotionRepository.findByName("기본")).thenReturn(defaultEmotion);

        // when
        TimelineResponseDto result = timelineService.getTimeline(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTrips()).hasSize(1);
        // 감정 정보가 없으면 기본 감정 정보 반환
        assertThat(result.getTrips().get(0).getEmotionName()).isEqualTo("기본");
        assertThat(result.getTrips().get(0).getEmotionColor()).isEqualTo("#EEEEEE");
    }
}

