package zim.tave.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.domain.TripTheme;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.CreatePastTripRequest;
import zim.tave.memory.dto.request.CreateTripRequest;
import zim.tave.memory.dto.response.TripResponseDto;
import zim.tave.memory.dto.request.UpdateTripRequest;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.DiaryRepository;
import zim.tave.memory.repository.TripRepository;
import zim.tave.memory.repository.TripThemeRepository;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripThemeRepository tripThemeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VisitedCountryService visitedCountryService;

    @Mock
    private DiaryRepository diaryRepository;

    @InjectMocks
    private TripService tripService;

    private User user;
    private TripTheme tripTheme;
    private Trip trip;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        tripTheme = new TripTheme();
        tripTheme.setId(1L);
        tripTheme.setThemeName("여름 테마");

        trip = new Trip();
        trip.setId(1L);
        trip.setTripName("제주도 여행");
        trip.setDescription("제주도 3박 4일 여행");
        trip.setUser(user);
        trip.setTripTheme(tripTheme);
        trip.setStartDate(LocalDate.of(2024, 1, 1));
        trip.setEndDate(LocalDate.of(2024, 1, 5));

        tripService.setSelf(tripService);
    }

    @Test
    void 여행_생성_테스트() {
        // given
        CreateTripRequest request = new CreateTripRequest();
        request.setTripName("제주도 여행");
        request.setDescription("제주도 3박 4일 여행");
        request.setThemeId(1L);
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 1, 5));

        when(tripThemeRepository.findById(1L)).thenReturn(Optional.of(tripTheme));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // when
        TripResponseDto result = tripService.createTrip(request, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTripName()).isEqualTo("제주도 여행");
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void 여행_생성_시_테마_없음_예외_테스트() {
        // given
        CreateTripRequest request = new CreateTripRequest();
        request.setTripName("제주도 여행");
        request.setDescription("설명");
        request.setThemeId(999L);
        request.setStartDate(LocalDate.of(2024, 2, 1));
        request.setEndDate(LocalDate.of(2024, 2, 10));

        when(tripThemeRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripService.createTrip(request, 1L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 여행_생성_검증_실패_여행명_누락() {
        // given
        CreateTripRequest request = new CreateTripRequest();
        request.setTripName("   ");
        request.setStartDate(LocalDate.of(2024, 3, 1));
        request.setEndDate(LocalDate.of(2024, 3, 5));
        // when & then
        assertThatThrownBy(() -> tripService.createTrip(request, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRIP_NAME_REQUIRED);
    }

    @Test
    void 여행_수정_테스트() {
        // given
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 여행명");
        request.setDescription("수정된 설명");

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when
        tripService.updateTrip(1L, request, 1L);

        // then
        verify(tripRepository).findById(1L);
        assertThat(trip.getTripName()).isEqualTo("수정된 여행명");
        assertThat(trip.getDescription()).isEqualTo("수정된 설명");
    }

    @Test
    void 여행_수정_시_테마_변경_테스트() {
        // given
        TripTheme newTheme = new TripTheme();
        newTheme.setId(2L);
        newTheme.setThemeName("겨울 테마");

        UpdateTripRequest request = new UpdateTripRequest();
        request.setThemeId(2L);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripThemeRepository.findById(2L)).thenReturn(Optional.of(newTheme));

        // when
        tripService.updateTrip(1L, request, 1L);

        // then
        assertThat(trip.getTripTheme()).isEqualTo(newTheme);
    }

    @Test
    void 여행_수정_검증_실패_길이초과_및_날짜역전() {
        // given
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("이름이너무너무길어서열네자를초과합니다");
        request.setDescription("x".repeat(57));
        request.setStartDate(LocalDate.of(2024, 2, 1));
        request.setEndDate(LocalDate.of(2024, 1, 1));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        // when & then
        assertThatThrownBy(() -> tripService.updateTrip(1L, request, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void 사용자별_여행_DTO_조회_테스트() {
        // given
        List<Trip> trips = Arrays.asList(trip);
        when(tripRepository.findByUserIdWithDiaries(1L)).thenReturn(trips);

        // when
        List<TripResponseDto> result = tripService.getTripsByUserId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTripName()).isEqualTo("제주도 여행");
    }

    @Test
    void 소유권_검증_실패시_FORBIDDEN() {
        // given
        User other = new User();
        other.setId(2L);
        trip.setUser(other);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        // when & then
        assertThatThrownBy(() -> tripService.getTripDtoWithOwnershipCheck(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRIP_UPDATE_FORBIDDEN);
    }

    @Test
    void 여행_대표이미지_설정_소유권_및_imageId_null_검증() {
        // imageId null
        assertThatThrownBy(() -> tripService.updateTripRepresentativeImageWithOwnershipCheck(1L, null, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_ID_REQUIRED);
    }

//    @Test
//    void 여행_삭제_테스트() {
//        // given
//        when(tripRepository.findOne(1L)).thenReturn(trip);
//
//        // when
//        tripService.deleteTrip(1L);
//
//        // then
//        assertThat(trip.getIsDeleted()).isTrue();
//    }

    @Test
    void 여행_종료날짜_업데이트_테스트() {
        // when
        tripService.updateTripEndDate(1L);

        // then
        verify(tripRepository).updateTripEndDate(1L);
    }

    @Test
    void 마지막_다이어리_날짜_조회_테스트() {
        // given
        LocalDate expectedDate = LocalDate.of(2024, 1, 15);
        when(tripRepository.findLastDiaryDateByTripId(1L)).thenReturn(expectedDate);

        // when
        LocalDate result = tripService.getLastDiaryDateByTripId(1L);

        // then
        assertThat(result).isEqualTo(expectedDate);
    }

    @Test
    void 과거_여행_생성_테스트() {
        // given
        CreatePastTripRequest request = new CreatePastTripRequest();
        request.setTripName("유럽 배낭여행");
        request.setDescription("2주간 여행");
        request.setStartDate(LocalDate.of(2023, 5, 1));
        request.setEndDate(LocalDate.of(2023, 5, 14));
        request.setCountryCodes(Arrays.asList("FR", "IT"));
        request.setEmotionId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tripThemeRepository.findById(1L)).thenReturn(Optional.of(tripTheme));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // when
        TripResponseDto result = tripService.createPastTrip(request, 1L);

        // then
        assertThat(result.getTripName()).isEqualTo("유럽 배낭여행");
        assertThat(result.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(result.getEndDate()).isEqualTo(request.getEndDate());
        verify(visitedCountryService).registerVisitedCountry(1L, "FR", 2L);
        verify(visitedCountryService).registerVisitedCountry(1L, "IT", 2L);
    }

    @Test
    void 여행_보관상태_변경_테스트() {
        // given
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when
        tripService.storeTrip(1L, 1L, true);

        // then
        assertThat(trip.getIsStored()).isTrue();
    }

    @Test
    void 여행_보관상태_변경_소유권_검증() {
        // given
        User other = new User();
        other.setId(2L);
        trip.setUser(other);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> tripService.storeTrip(1L, 1L, true))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRIP_UPDATE_FORBIDDEN);
    }

    @Test
    void 과거_여행_수정_성공() {
        // given
        trip.setIsPast(true);
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 과거 여행명");
        request.setDescription("수정된 설명");

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when
        tripService.updatePastTrip(1L, request, 1L);

        // then
        assertThat(trip.getTripName()).isEqualTo("수정된 과거 여행명");
        assertThat(trip.getDescription()).isEqualTo("수정된 설명");
    }

    @Test
    void 과거_여행_수정_실패_과거_여행_아님() {
        // given
        trip.setIsPast(false); // 일반 여행
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 여행명");

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> tripService.updatePastTrip(1L, request, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_PAST_TRIP);
    }

    @Test
    void 과거_여행_보관_성공() {
        // given
        trip.setIsPast(true);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when
        tripService.storePastTrip(1L, 1L, true);

        // then
        assertThat(trip.getIsStored()).isTrue();
    }

    @Test
    void 과거_여행_보관_실패_과거_여행_아님() {
        // given
        trip.setIsPast(false); // 일반 여행
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> tripService.storePastTrip(1L, 1L, true))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_PAST_TRIP);
    }

    @Test
    void 과거_여행_삭제_성공() {
        // given
        trip.setIsPast(true);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when
        tripService.deletePastTrip(1L, 1L);

        // then
        verify(tripRepository).delete(trip);
    }

    @Test
    void 과거_여행_삭제_실패_과거_여행_아님() {
        // given
        trip.setIsPast(false); // 일반 여행
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> tripService.deletePastTrip(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_PAST_TRIP);
    }

    @Test
    void 과거_여행_수정_소유권_검증() {
        // given
        trip.setIsPast(true);
        User other = new User();
        other.setId(2L);
        trip.setUser(other);
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 여행명");

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> tripService.updatePastTrip(1L, request, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRIP_UPDATE_FORBIDDEN);
    }

    @Test
    void 날짜_검증_시작날짜가_null인_경우() {
        // given
        trip.setStartDate(null);
        UpdateTripRequest request = new UpdateTripRequest();
        request.setEndDate(LocalDate.of(2024, 2, 1));

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> tripService.updateTrip(1L, request, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void 날짜_검증_종료날짜가_null인_경우() {
        // given
        trip.setEndDate(null);
        UpdateTripRequest request = new UpdateTripRequest();
        request.setStartDate(LocalDate.of(2024, 2, 1));

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> tripService.updateTrip(1L, request, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void 날짜_검증_시작날짜만_업데이트() {
        // given
        UpdateTripRequest request = new UpdateTripRequest();
        request.setStartDate(LocalDate.of(2024, 1, 2));
        // endDate는 null (기존 값 유지)

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when
        tripService.updateTrip(1L, request, 1L);

        // then
        assertThat(trip.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(trip.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 5)); // 기존 값 유지
    }

    @Test
    void 날짜_검증_종료날짜만_업데이트() {
        // given
        UpdateTripRequest request = new UpdateTripRequest();
        request.setEndDate(LocalDate.of(2024, 1, 10));
        // startDate는 null (기존 값 유지)

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when
        tripService.updateTrip(1L, request, 1L);

        // then
        assertThat(trip.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1)); // 기존 값 유지
        assertThat(trip.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 10));
    }

    @Test
    void 날짜_검증_시작종료_날짜_동일() {
        // given
        UpdateTripRequest request = new UpdateTripRequest();
        request.setStartDate(LocalDate.of(2024, 1, 5));
        request.setEndDate(LocalDate.of(2024, 1, 5));

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when
        tripService.updateTrip(1L, request, 1L);

        // then
        assertThat(trip.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 5));
        assertThat(trip.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 5));
    }

    @Test
    void 과거_여행_보관_시_self_트랜잭션_호출() {
        // given
        trip.setIsPast(true);
        TripService spyService = spy(tripService);
        spyService.setSelf(spyService);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // when
        spyService.storePastTrip(1L, 1L, true);

        // then
        verify(spyService).storeTrip(1L, 1L, true); // self를 통해 호출되었는지 확인
    }

}
