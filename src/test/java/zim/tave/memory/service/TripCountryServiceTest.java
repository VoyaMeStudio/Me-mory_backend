package zim.tave.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.domain.TripCountry;
import zim.tave.memory.repository.TripCountryRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripCountryServiceTest {

    @Mock
    private TripCountryRepository tripCountryRepository;

    @Mock
    private CountryService countryService;

    @InjectMocks
    private TripCountryService tripCountryService;

    private Trip trip;
    private Country korea;

    @BeforeEach
    void setUp() {
        trip = new Trip();
        trip.setId(1L);

        korea = new Country();
        korea.setCountryCode("KR");
        korea.setCountryName("대한민국");
    }

    @Test
    void 여행_국가_목록이_null이면_등록하지_않음() {
        // when
        tripCountryService.registerTripCountries(trip, null);

        // then
        verifyNoInteractions(countryService, tripCountryRepository);
    }

    @Test
    void 여행_국가_목록이_비어있으면_등록하지_않음() {
        // when
        tripCountryService.registerTripCountries(trip, List.of());

        // then
        verifyNoInteractions(countryService, tripCountryRepository);
    }

    @Test
    void 여행_국가_목록_등록_시_해결된_국가코드가_중복이면_한번만_저장() {
        // given
        when(countryService.findByCode("KR")).thenReturn(korea);
        when(tripCountryRepository.existsByTrip_IdAndCountry_CountryCode(1L, "KR"))
                .thenReturn(false);

        // when
        tripCountryService.registerTripCountries(trip, List.of("KR", "KR"));

        // then
        verify(countryService, times(2)).findByCode("KR");
        verify(tripCountryRepository, times(1))
                .existsByTrip_IdAndCountry_CountryCode(1L, "KR");
        verify(tripCountryRepository, times(1)).save(any(TripCountry.class));
    }

    @Test
    void 여행_국가가_이미_등록되어_있으면_저장하지_않음() {
        // given
        when(tripCountryRepository.existsByTrip_IdAndCountry_CountryCode(1L, "KR"))
                .thenReturn(true);

        // when
        tripCountryService.registerTripCountry(trip, korea);

        // then
        verify(tripCountryRepository).existsByTrip_IdAndCountry_CountryCode(1L, "KR");
        verify(tripCountryRepository, never()).save(any(TripCountry.class));
    }

    @Test
    void 여행_국가가_없으면_새로_저장() {
        // given
        when(tripCountryRepository.existsByTrip_IdAndCountry_CountryCode(1L, "KR"))
                .thenReturn(false);

        // when
        tripCountryService.registerTripCountry(trip, korea);

        // then
        ArgumentCaptor<TripCountry> captor = ArgumentCaptor.forClass(TripCountry.class);
        verify(tripCountryRepository).save(captor.capture());

        TripCountry saved = captor.getValue();
        assertThat(saved.getTrip()).isSameAs(trip);
        assertThat(saved.getCountry()).isSameAs(korea);
    }

    @Test
    void 여행_국가_등록_방어로직은_저장소를_호출하지_않음() {
        // given
        Trip tripWithoutId = new Trip();
        Country countryWithoutCode = new Country();

        // when
        tripCountryService.registerTripCountry(null, korea);
        tripCountryService.registerTripCountry(tripWithoutId, korea);
        tripCountryService.registerTripCountry(trip, null);
        tripCountryService.registerTripCountry(trip, countryWithoutCode);

        // then
        verifyNoInteractions(tripCountryRepository);
    }

    @Test
    void 여행_국가_삭제_대상이_있으면_삭제() {
        // given
        TripCountry tripCountry = TripCountry.create(trip, korea);
        when(tripCountryRepository.findByTrip_IdAndCountry_CountryCode(1L, "KR"))
                .thenReturn(Optional.of(tripCountry));

        // when
        tripCountryService.deleteTripCountry(trip, korea);

        // then
        verify(tripCountryRepository).delete(tripCountry);
    }

    @Test
    void 여행_국가_삭제_대상이_없으면_삭제하지_않음() {
        // given
        when(tripCountryRepository.findByTrip_IdAndCountry_CountryCode(1L, "KR"))
                .thenReturn(Optional.empty());

        // when
        tripCountryService.deleteTripCountry(trip, korea);

        // then
        verify(tripCountryRepository, never()).delete(any(TripCountry.class));
    }

    @Test
    void 여행_국가_삭제_방어로직은_저장소를_호출하지_않음() {
        // given
        Trip tripWithoutId = new Trip();
        Country countryWithoutCode = new Country();

        // when
        tripCountryService.deleteTripCountry(null, korea);
        tripCountryService.deleteTripCountry(tripWithoutId, korea);
        tripCountryService.deleteTripCountry(trip, null);
        tripCountryService.deleteTripCountry(trip, countryWithoutCode);

        // then
        verifyNoInteractions(tripCountryRepository);
    }
}
