package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.Diary;
import zim.tave.memory.domain.DiaryImage;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.domain.VisitedCountry;
import zim.tave.memory.dto.TimelineTripDto;
import zim.tave.memory.dto.VisitedCountryInfoDto;
import zim.tave.memory.dto.response.TimelineResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.TripRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimelineService {

    private final TripRepository tripRepository;
    private final VisitedCountryService visitedCountryService;

    public TimelineResponseDto getTimeline(Long userId) {
        ensureAuthenticated(userId);

        List<Trip> trips = tripRepository.findActiveTripsWithDetails(userId);
        Map<String, VisitedCountry> visitedCountryMap = visitedCountryService.getVisitedCountryMap(userId);

        List<TimelineTripDto> timelineTrips = trips.stream()
                .filter(this::isActiveTrip)
                .sorted(Comparator.comparing(
                                Trip::getStartDate,
                                Comparator.nullsLast(LocalDate::compareTo))
                        .reversed())
                .map(trip -> toTimelineTripDto(trip, visitedCountryMap))
                .toList();

        return TimelineResponseDto.builder()
                .trips(timelineTrips)
                .build();
    }

    private TimelineTripDto toTimelineTripDto(Trip trip, Map<String, VisitedCountry> visitedCountryMap) {
        return TimelineTripDto.builder()
                .tripId(trip.getId())
                .tripName(trip.getTripName())
                .description(trip.getDescription())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .isPast(Boolean.TRUE.equals(trip.getIsPast()))
                .representativeImageUrl(resolveRepresentativeImage(trip))
                .visitedCountries(collectVisitedCountries(trip, visitedCountryMap))
                .build();
    }

    private List<VisitedCountryInfoDto> collectVisitedCountries(Trip trip, Map<String, VisitedCountry> visitedCountryMap) {
        List<Diary> diaries = filterActiveDiaries(trip.getDiaries());
        if (diaries == null || diaries.isEmpty()) {
            return List.of();
        }

        Map<String, Country> uniqueCountries = diaries.stream()
                .map(Diary::getCountry)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Country::getCountryCode,
                        country -> country,
                        (existing, duplicate) -> existing,
                        LinkedHashMap::new
                ));

        return uniqueCountries.values().stream()
                .map(country -> buildVisitedCountryInfo(country, visitedCountryMap.get(country.getCountryCode())))
                .toList();
    }

    private VisitedCountryInfoDto buildVisitedCountryInfo(Country country, VisitedCountry visitedCountry) {
        String emotionName = visitedCountry != null && visitedCountry.getEmotion() != null
                ? visitedCountry.getEmotion().getName()
                : null;
        String emotionColor = visitedCountry != null ? visitedCountry.getColor() : null;

        return VisitedCountryInfoDto.builder()
                .countryCode(country.getCountryCode())
                .countryName(country.getCountryName())
                .emoji(country.getEmoji())
                .emotionName(emotionName)
                .emotionColor(emotionColor)
                .build();
    }

    private String resolveRepresentativeImage(Trip trip) {
        String representativeImageUrl = trip.getRepresentativeImageUrl();
        if (representativeImageUrl != null && !representativeImageUrl.trim().isEmpty()) {
            return representativeImageUrl;
        }

        List<Diary> diaries = filterActiveDiaries(trip.getDiaries());
        if (diaries == null || diaries.isEmpty()) {
            return null;
        }

        return diaries.stream()
                .sorted(Comparator.comparing(Diary::getCreatedAt))
                .map(this::findRepresentativeImageUrl)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
    }

    private Optional<String> findRepresentativeImageUrl(Diary diary) {
        return diary.getDiaryImages().stream()
                .sorted(Comparator.comparingInt(DiaryImage::getImageOrder))
                .filter(DiaryImage::isRepresentative)
                .map(DiaryImage::getImageUrl)
                .findFirst();
    }

    private List<Diary> filterActiveDiaries(List<Diary> diaries) {
        if (diaries == null || diaries.isEmpty()) {
            return List.of();
        }
        return diaries.stream()
                .filter(this::isActiveDiary)
                .toList();
    }

    private boolean isActiveTrip(Trip trip) {
        return !Boolean.TRUE.equals(trip.getIsStored());
    }

    private boolean isActiveDiary(Diary diary) {
        return !Boolean.TRUE.equals(diary.getIsStored());
    }

    private void ensureAuthenticated(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }
}

