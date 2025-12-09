package zim.tave.memory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import zim.tave.memory.domain.*;
import zim.tave.memory.dto.request.CreateDiaryRequest;
import zim.tave.memory.dto.response.DiaryResponseDto;
import zim.tave.memory.dto.request.UpdateDiaryOptionalFieldsRequest;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.DiaryRepository;
import zim.tave.memory.repository.DiaryImageRepository;
import zim.tave.memory.repository.TripRepository;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.repository.EmotionRepository;
import zim.tave.memory.repository.WeatherRepository;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private EmotionRepository emotionRepository;

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private DiaryImageRepository diaryImageRepository;

    @Mock
    private CountryService countryService;

    @Mock
    private VisitedCountryService visitedCountryService;

    @InjectMocks
    private DiaryService diaryService;

    private User user;
    private Trip trip;
    private Diary diary;
    private Emotion emotion;
    private Weather weather;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        trip = new Trip();
        trip.setId(1L);

        diary = new Diary();
        diary.setId(1L);
        diary.setUser(user);
        diary.setTrip(trip);
        diary.setCity("서울");
        diary.setContent("테스트 내용");
        diary.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        diary.setIsStored(false);

        emotion = new Emotion("행복", "#FFD700");
        emotion.setId(1L);

        weather = new Weather();
        weather.setId(1L);
        weather.setName("맑음");
    }

    @Test
    void 다이어리_생성_테스트() {
        // given
        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setUserId(1L);
        request.setTripId(1L);
        request.setCountryCode("KR");
        request.setCity("서울");
        request.setDateTime("2024-01-15T10:00:00");
        request.setContent("테스트 내용");

        CreateDiaryRequest.DiaryImageInfo imageInfo1 = new CreateDiaryRequest.DiaryImageInfo();
        imageInfo1.setImageUrl("front.jpg");
        imageInfo1.setCameraType(DiaryImage.CameraType.FRONT);
        imageInfo1.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo imageInfo2 = new CreateDiaryRequest.DiaryImageInfo();
        imageInfo2.setImageUrl("back.jpg");
        imageInfo2.setCameraType(DiaryImage.CameraType.BACK);
        imageInfo2.setRepresentative(false);

        request.setImages(Arrays.asList(imageInfo1, imageInfo2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        // defaults for country/emotion
        Country country = new Country();
        country.setCountryCode("KR");
        country.setCountryName("대한민국");
        when(countryService.findByCode("KR")).thenReturn(country);
        when(emotionRepository.findById(1L)).thenReturn(Optional.of(emotion));
        when(diaryRepository.save(any(Diary.class))).thenAnswer(invocation -> {
            Diary saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        // when
        DiaryResponseDto result = diaryService.createDiary(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo("서울");
        assertThat(result.getContent()).isEqualTo("테스트 내용");
        verify(diaryRepository).save(any(Diary.class));
    }

    @Test
    void 다이어리_생성_시_사용자_없음_예외_테스트() {
        // given
        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setUserId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.createDiary(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 선택적_필드_업데이트_테스트() {
        // given
        UpdateDiaryOptionalFieldsRequest request = new UpdateDiaryOptionalFieldsRequest();
        request.setDetailedLocation("강남역 1번 출구");
        request.setEmotionId(1L);
        request.setWeatherId(1L);

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        when(emotionRepository.findById(1L)).thenReturn(Optional.of(emotion));
        when(weatherRepository.findById(1L)).thenReturn(Optional.of(weather));

        // when
        diaryService.updateDiaryOptionalFields(1L, 1L, request);

        // then
        assertThat(diary.getDetailedLocation()).isEqualTo("강남역 1번 출구");
        assertThat(diary.getEmotion()).isEqualTo(emotion);
        assertThat(diary.getWeather()).isEqualTo(weather);
    }

    @Test
    void 대표사진_변경_테스트() {
        // given
        DiaryImage image1 = new DiaryImage();
        image1.setId(1L);
        image1.setRepresentative(true);

        DiaryImage image2 = new DiaryImage();
        image2.setId(2L);
        image2.setRepresentative(false);

        diary.addDiaryImage(image1);
        diary.addDiaryImage(image2);

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        when(diaryImageRepository.findById(2L)).thenReturn(Optional.of(image2));

        // when
        diaryService.updateRepresentativeImage(1L, 2L);

        // then
        assertThat(image1.isRepresentative()).isFalse();
        assertThat(image2.isRepresentative()).isTrue();
    }

    @Test
    void 대표사진_변경_시_이미지_없음_예외_테스트() {
        // given
        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        when(diaryImageRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.updateRepresentativeImage(1L, 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_NOT_FOUND);
    }

    @Test
    void 다이어리_삭제_테스트() {
        // given
        trip.setStartDate(LocalDate.of(2024, 1, 1));
        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(diaryRepository.findByTrip_Id(1L)).thenReturn(Arrays.asList());

        // when
        diaryService.deleteDiary(1L, 1L);

        // then
        verify(diaryRepository).delete(diary);
        assertThat(trip.getEndDate()).isEqualTo(trip.getStartDate());
    }

    @Test
    void 다이어리_삭제_후_남은_다이어리_있을_때_종료날짜_업데이트_테스트() {
        // given
        Diary remainingDiary = new Diary();
        remainingDiary.setCreatedAt(LocalDateTime.of(2024, 1, 20, 10, 0));

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(diaryRepository.findByTrip_Id(1L)).thenReturn(Arrays.asList(remainingDiary));

        // when
        diaryService.deleteDiary(1L, 1L);

        // then
        verify(diaryRepository).delete(diary);
        assertThat(trip.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 20));
    }

    @Test
    void 다이어리_삭제_소유권_검증_실패_테스트() {
        // given
        User otherUser = new User();
        otherUser.setId(2L);
        diary.setUser(otherUser);

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    void 다이어리_삭제_인증_실패_테스트() {
        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(1L, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    void 사용자별_다이어리_조회_테스트() {
        // given
        List<Diary> diaries = Arrays.asList(diary);
        when(diaryRepository.findByUser_Id(1L)).thenReturn(diaries);

        // when
        List<DiaryResponseDto> result = diaryService.findByUserId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("서울");
    }

    @Test
    void 여행별_다이어리_조회_테스트() {
        // given
        List<Diary> diaries = Arrays.asList(diary);
        when(diaryRepository.findByTrip_Id(1L)).thenReturn(diaries);

        // when
        List<DiaryResponseDto> result = diaryService.findByTripId(1L, 1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("서울");
    }

    @Test
    void 다이어리_보관_테스트() {
        // given
        diary.setIsStored(false);
        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));

        // when
        diaryService.storeDiary(1L, 1L, true);

        // then
        assertThat(diary.getIsStored()).isTrue();
    }

    @Test
    void 다이어리_보관_해제_테스트() {
        // given
        diary.setIsStored(true);
        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));

        // when
        diaryService.storeDiary(1L, 1L, false);

        // then
        assertThat(diary.getIsStored()).isFalse();
    }

    @Test
    void 다이어리_보관_소유권_검증_실패_테스트() {
        // given
        User otherUser = new User();
        otherUser.setId(2L);
        diary.setUser(otherUser);

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));

        // when & then
        assertThatThrownBy(() -> diaryService.storeDiary(1L, 1L, true))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    void 다이어리_보관_인증_실패_테스트() {
        // when & then
        assertThatThrownBy(() -> diaryService.storeDiary(1L, null, true))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    void 다이어리_보관_다이어리_없음_예외_테스트() {
        // given
        when(diaryRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.storeDiary(999L, 1L, true))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIARY_NOT_FOUND);
    }

    @Test
    void 사용자별_다이어리_조회_시_숨긴_다이어리_제외_테스트() {
        // given
        Diary activeDiary = new Diary();
        activeDiary.setId(1L);
        activeDiary.setUser(user);
        activeDiary.setIsStored(false);

        Diary storedDiary = new Diary();
        storedDiary.setId(2L);
        storedDiary.setUser(user);
        storedDiary.setIsStored(true);

        // findByUser_Id는 isStored = false인 것만 반환하도록 필터링됨
        when(diaryRepository.findByUser_Id(1L)).thenReturn(Arrays.asList(activeDiary));

        // when
        List<DiaryResponseDto> result = diaryService.findByUserId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(diaryRepository).findByUser_Id(1L);
    }

    @Test
    void 여행별_다이어리_조회_시_숨긴_다이어리_제외_테스트() {
        // given
        Diary activeDiary = new Diary();
        activeDiary.setId(1L);
        activeDiary.setUser(user);
        activeDiary.setTrip(trip);
        activeDiary.setIsStored(false);

        Diary storedDiary = new Diary();
        storedDiary.setId(2L);
        storedDiary.setUser(user);
        storedDiary.setTrip(trip);
        storedDiary.setIsStored(true);

        // findByTrip_Id는 isStored = false인 것만 반환하도록 필터링됨
        when(diaryRepository.findByTrip_Id(1L)).thenReturn(Arrays.asList(activeDiary));

        // when
        List<DiaryResponseDto> result = diaryService.findByTripId(1L, 1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(diaryRepository).findByTrip_Id(1L);
    }

    @Test
    void 날짜시간_파싱_Z로_끝나는_ISO8601_포맷_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - Z로 끝나는 포맷
        LocalDateTime result1 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.570Z");
        assertThat(result1).isNotNull();
        
        LocalDateTime result2 = (LocalDateTime) parseDateTime.invoke(diaryService, "2024-01-15T10:00:00.000Z");
        assertThat(result2).isNotNull();
        assertThat(result2.getYear()).isEqualTo(2024);
        assertThat(result2.getMonthValue()).isEqualTo(1);
        assertThat(result2.getDayOfMonth()).isEqualTo(15);
    }

    @Test
    void 날짜시간_파싱_Offset_포함_포맷_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - +09:00 (한국 시간)
        LocalDateTime result1 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01+09:00");
        assertThat(result1).isNotNull();
        
        // when & then - -05:00 (미국 동부 시간)
        LocalDateTime result2 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01-05:00");
        assertThat(result2).isNotNull();
        
        // when & then - +00:00 (UTC)
        LocalDateTime result3 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01+00:00");
        assertThat(result3).isNotNull();
        
        // when & then - 밀리초와 offset 함께
        LocalDateTime result4 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.123+09:00");
        assertThat(result4).isNotNull();
    }

    @Test
    void 날짜시간_파싱_밀리초_포함_포맷_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - 3자리 밀리초
        LocalDateTime result1 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.123");
        assertThat(result1).isNotNull();
        assertThat(result1.getNano()).isEqualTo(123000000);
        
        // when & then - 1자리 밀리초
        LocalDateTime result2 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.1");
        assertThat(result2).isNotNull();
        
        // when & then - 2자리 밀리초
        LocalDateTime result3 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.12");
        assertThat(result3).isNotNull();
        
        // when & then - 6자리 밀리초 (나노초까지)
        LocalDateTime result4 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.123456");
        assertThat(result4).isNotNull();
    }

    @Test
    void 날짜시간_파싱_기본_형식_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - 기본 형식 (밀리초 없음)
        LocalDateTime result = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01");
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2025);
        assertThat(result.getMonthValue()).isEqualTo(11);
        assertThat(result.getDayOfMonth()).isEqualTo(30);
        assertThat(result.getHour()).isEqualTo(11);
        assertThat(result.getMinute()).isEqualTo(20);
        assertThat(result.getSecond()).isEqualTo(1);
    }

    @Test
    void 날짜시간_파싱_공백_제거_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - 앞뒤 공백 제거
        LocalDateTime result1 = (LocalDateTime) parseDateTime.invoke(diaryService, "  2025-11-30T11:20:01  ");
        assertThat(result1).isNotNull();
        
        LocalDateTime result2 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01");
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void 날짜시간_파싱_null_또는_빈문자열_예외_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - null
        assertThatThrownBy(() -> parseDateTime.invoke(diaryService, (String) null))
                .hasCauseInstanceOf(CustomException.class);
        
        // when & then - 빈 문자열
        assertThatThrownBy(() -> parseDateTime.invoke(diaryService, ""))
                .hasCauseInstanceOf(CustomException.class);
        
        // when & then - 공백만 있는 문자열
        assertThatThrownBy(() -> parseDateTime.invoke(diaryService, "   "))
                .hasCauseInstanceOf(CustomException.class);
    }

    @Test
    void 날짜시간_파싱_잘못된_형식_예외_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - 잘못된 형식들
        assertThatThrownBy(() -> parseDateTime.invoke(diaryService, "2025-11-30"))
                .hasCauseInstanceOf(CustomException.class);
        
        assertThatThrownBy(() -> parseDateTime.invoke(diaryService, "2025/11/30 11:20:01"))
                .hasCauseInstanceOf(CustomException.class);
        
        assertThatThrownBy(() -> parseDateTime.invoke(diaryService, "invalid-date"))
                .hasCauseInstanceOf(CustomException.class);
        
        assertThatThrownBy(() -> parseDateTime.invoke(diaryService, "2025-13-45T25:70:99"))
                .hasCauseInstanceOf(CustomException.class);
    }

    @Test
    void 날짜시간_파싱_다양한_실제_사용_시나리오_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - JavaScript Date.toISOString() 결과 (Z 포함)
        LocalDateTime result1 = (LocalDateTime) parseDateTime.invoke(diaryService, "2024-12-25T15:30:45.789Z");
        assertThat(result1).isNotNull();
        
        // when & then - 모바일 앱에서 보낼 수 있는 offset 형식
        LocalDateTime result2 = (LocalDateTime) parseDateTime.invoke(diaryService, "2024-12-25T15:30:45+09:00");
        assertThat(result2).isNotNull();
        
        // when & then - 서버에서 생성한 로컬 시간
        LocalDateTime result3 = (LocalDateTime) parseDateTime.invoke(diaryService, "2024-12-25T15:30:45");
        assertThat(result3).isNotNull();
        
        // when & then - 밀리초 포함 로컬 시간
        LocalDateTime result4 = (LocalDateTime) parseDateTime.invoke(diaryService, "2024-12-25T15:30:45.123");
        assertThat(result4).isNotNull();
    }

    @Test
    void 날짜시간_파싱_Edge_Case_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - 자정
        LocalDateTime result1 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-01-01T00:00:00");
        assertThat(result1).isNotNull();
        assertThat(result1.getHour()).isZero();
        assertThat(result1.getMinute()).isZero();
        assertThat(result1.getSecond()).isZero();
        
        // when & then - 23:59:59
        LocalDateTime result2 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-12-31T23:59:59");
        assertThat(result2).isNotNull();
        assertThat(result2.getHour()).isEqualTo(23);
        assertThat(result2.getMinute()).isEqualTo(59);
        assertThat(result2.getSecond()).isEqualTo(59);
        
        // when & then - 윤년 2월 29일
        LocalDateTime result3 = (LocalDateTime) parseDateTime.invoke(diaryService, "2024-02-29T12:00:00");
        assertThat(result3).isNotNull();
        assertThat(result3.getMonthValue()).isEqualTo(2);
        assertThat(result3.getDayOfMonth()).isEqualTo(29);
    }

    @Test
    void 날짜시간_파싱_다양한_밀리초_자릿수_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - 1자리 밀리초
        LocalDateTime result1 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.1");
        assertThat(result1).isNotNull();
        
        // when & then - 2자리 밀리초
        LocalDateTime result2 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.12");
        assertThat(result2).isNotNull();
        
        // when & then - 3자리 밀리초
        LocalDateTime result3 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.123");
        assertThat(result3).isNotNull();
        
        // when & then - 4자리 밀리초
        LocalDateTime result4 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.1234");
        assertThat(result4).isNotNull();
        
        // when & then - 5자리 밀리초
        LocalDateTime result5 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.12345");
        assertThat(result5).isNotNull();
        
        // when & then - 6자리 밀리초
        LocalDateTime result6 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.123456");
        assertThat(result6).isNotNull();
    }

    @Test
    void 날짜시간_파싱_Offset과_밀리초_조합_테스트() throws Exception {
        // given
        Method parseDateTime = DiaryService.class.getDeclaredMethod("parseDateTime", String.class);
        parseDateTime.setAccessible(true);
        
        // when & then - Z와 밀리초
        LocalDateTime result1 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.123Z");
        assertThat(result1).isNotNull();
        
        // when & then - +09:00와 밀리초
        LocalDateTime result2 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.456+09:00");
        assertThat(result2).isNotNull();
        
        // when & then - -05:00와 밀리초
        LocalDateTime result3 = (LocalDateTime) parseDateTime.invoke(diaryService, "2025-11-30T11:20:01.789-05:00");
        assertThat(result3).isNotNull();
    }

    @Test
    void 날짜시간_파싱_실제_다이어리_생성_통합_테스트() {
        // given
        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setUserId(1L);
        request.setTripId(1L);
        request.setCountryCode("KR");
        request.setCity("서울");
        request.setContent("테스트 내용");
        
        CreateDiaryRequest.DiaryImageInfo imageInfo1 = new CreateDiaryRequest.DiaryImageInfo();
        imageInfo1.setImageUrl("front.jpg");
        imageInfo1.setCameraType(DiaryImage.CameraType.FRONT);
        imageInfo1.setRepresentative(true);
        
        CreateDiaryRequest.DiaryImageInfo imageInfo2 = new CreateDiaryRequest.DiaryImageInfo();
        imageInfo2.setImageUrl("back.jpg");
        imageInfo2.setCameraType(DiaryImage.CameraType.BACK);
        imageInfo2.setRepresentative(false);
        
        request.setImages(Arrays.asList(imageInfo1, imageInfo2));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        trip.setIsPast(false);
        
        Country country = new Country();
        country.setCountryCode("KR");
        country.setCountryName("대한민국");
        when(countryService.findByCode("KR")).thenReturn(country);
        when(emotionRepository.findById(1L)).thenReturn(Optional.of(emotion));
        when(diaryRepository.save(any(Diary.class))).thenAnswer(invocation -> {
            Diary saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        
        // when & then - Z 포맷으로 다이어리 생성
        request.setDateTime("2025-11-30T11:20:01.570Z");
        DiaryResponseDto result1 = diaryService.createDiary(request);
        assertThat(result1).isNotNull();
        assertThat(result1.getDateTime()).isNotNull();
        
        // when & then - Offset 포맷으로 다이어리 생성
        request.setDateTime("2025-11-30T11:20:01+09:00");
        DiaryResponseDto result2 = diaryService.createDiary(request);
        assertThat(result2).isNotNull();
        assertThat(result2.getDateTime()).isNotNull();
        
        // when & then - 밀리초 포함 포맷으로 다이어리 생성
        request.setDateTime("2025-11-30T11:20:01.123");
        DiaryResponseDto result3 = diaryService.createDiary(request);
        assertThat(result3).isNotNull();
        assertThat(result3.getDateTime()).isNotNull();
        
        // when & then - 기본 포맷으로 다이어리 생성
        request.setDateTime("2025-11-30T11:20:01");
        DiaryResponseDto result4 = diaryService.createDiary(request);
        assertThat(result4).isNotNull();
        assertThat(result4.getDateTime()).isNotNull();
    }
}
