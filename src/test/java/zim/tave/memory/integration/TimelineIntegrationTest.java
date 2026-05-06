package zim.tave.memory.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.request.CreatePastTripRequest;
import zim.tave.memory.dto.response.TimelineResponseDto;
import zim.tave.memory.dto.response.TripResponseDto;
import zim.tave.memory.repository.VisitedCountryRepository;
import zim.tave.memory.service.TimelineService;
import zim.tave.memory.service.TripService;
import zim.tave.memory.service.VisitedCountryService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 타임라인 통합 테스트
 * - 타임라인 조회 (보관되지 않은 모든 여행 시간순)
 * - 타임라인에서 과거 여행 추가
 */
public class TimelineIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private TripService tripService;

    @Autowired
    private VisitedCountryService visitedCountryService;

    @Autowired
    protected VisitedCountryRepository visitedCountryRepository;

    @BeforeEach
    void setUp() {
        initializeTestData();
    }

    @Test
    void 타임라인_조회_보관되지_않은_여행만_표시_테스트() {
        // given - 활성 여행과 보관된 여행 생성
        Trip activeTrip = createTestTrip(
            "활성 여행",
            "활성 여행 설명",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 5)
        );

        Trip storedTrip = createTestTrip(
            "보관된 여행",
            "보관된 여행 설명",
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 2, 5)
        );
        storedTrip.setIsStored(true);
        tripRepository.save(storedTrip);

        // when
        TimelineResponseDto timeline = timelineService.getTimeline(testUser.getId());

        // then - 보관된 여행은 제외되어야 함
        assertThat(timeline.getTrips()).hasSize(1);
        assertThat(timeline.getTrips().get(0).getTripName()).isEqualTo("활성 여행");
    }

    @Test
    void 타임라인_시간순_정렬_테스트() {
        // given - 여러 여행 생성 (다른 시작일)
        Trip trip1 = createTestTrip(
            "첫 번째 여행",
            "설명",
            LocalDate.of(2024, 3, 1),
            LocalDate.of(2024, 3, 5)
        );

        Trip trip2 = createTestTrip(
            "두 번째 여행",
            "설명",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 5)
        );

        Trip trip3 = createTestTrip(
            "세 번째 여행",
            "설명",
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 2, 5)
        );

        // when
        TimelineResponseDto timeline = timelineService.getTimeline(testUser.getId());

        // then - 시작일 기준 내림차순 정렬 (가장 최근 여행이 첫 번째)
        assertThat(timeline.getTrips()).hasSize(3);
        assertThat(timeline.getTrips().get(0).getTripName()).isEqualTo("첫 번째 여행");
        assertThat(timeline.getTrips().get(1).getTripName()).isEqualTo("세 번째 여행");
        assertThat(timeline.getTrips().get(2).getTripName()).isEqualTo("두 번째 여행");
    }

    @Test
    void 타임라인에서_과거_여행_추가_테스트() {
        // given
        CreatePastTripRequest request = new CreatePastTripRequest();
        request.setTripName("유럽 여행");
        request.setDescription("2023년 유럽 배낭 여행");
        request.setStartDate(LocalDate.of(2023, 5, 1));
        request.setEndDate(LocalDate.of(2023, 5, 14));
        request.setCountryCodes(Arrays.asList("CN", "JP"));
        request.setEmotionId(defaultEmotion.getId());

        // when
        TripResponseDto createdPastTrip = tripService.createPastTrip(request, testUser.getId());

        // then
        assertThat(createdPastTrip).isNotNull();
        assertThat(createdPastTrip.getTripName()).isEqualTo("유럽 여행");
        
        Trip pastTrip = tripRepository.findById(createdPastTrip.getId()).orElseThrow();
        assertThat(pastTrip.getIsPast()).isTrue();
        assertThat(pastTrip.getIsStored()).isFalse();

        // 방문 국가가 등록되었는지 확인
        List<VisitedCountry> visitedCountries = visitedCountryRepository.findByUserIdWithDetails(testUser.getId());
        assertThat(visitedCountries).isNotEmpty();

        TimelineResponseDto timeline = timelineService.getTimeline(testUser.getId());
        assertThat(timeline.getTrips())
                .filteredOn(trip -> trip.getTripId().equals(createdPastTrip.getId()))
                .singleElement()
                .satisfies(trip -> assertThat(trip.getVisitedCountries())
                        .extracting("countryCode")
                        .containsExactlyInAnyOrder("CN", "JP"));
    }
}
