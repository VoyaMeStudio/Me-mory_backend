package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.Diary;
import zim.tave.memory.domain.DiaryImage;
import zim.tave.memory.domain.Emotion;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.dto.TimelineTripDto;
import zim.tave.memory.dto.VisitedCountryInfoDto;
import zim.tave.memory.dto.response.TimelineResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.EmotionRepository;
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
    private final EmotionRepository emotionRepository;

    /**
     * 사용자의 타임라인 조회(보관함에 숨긴 여행 제외, 시작일 기준 내림차순 정렬 )
    */
    public TimelineResponseDto getTimeline(Long userId) {
        ensureAuthenticated(userId);

        List<Trip> trips = tripRepository.findActiveTripsWithDetails(userId);

        // 활성 여행만 필터링하고 시작일 기준 내림차순 정렬
        List<TimelineTripDto> timelineTrips = trips.stream()
                .filter(this::isActiveTrip)
                .sorted(Comparator.comparing(
                                Trip::getStartDate,
                                Comparator.nullsLast(LocalDate::compareTo))
                        .reversed())
                .map(this::toTimelineTripDto)
                .toList();

        return TimelineResponseDto.builder()
                .trips(timelineTrips)
                .build();
    }

    /**
     * Trip 엔티티를 TimelineTripDto로 변환(가장 최근 일기에서 감정 정보 추출, 대표 이미지 URL 결정, 방문한 국가 목록 수집)
     */
    private TimelineTripDto toTimelineTripDto(Trip trip) {
        // 가장 최근 일기에서 감정 정보 추출
        TripEmotionInfo emotionInfo = resolveTripEmotion(trip);
        
        return TimelineTripDto.builder()
                .tripId(trip.getId())
                .tripName(trip.getTripName())
                .description(trip.getDescription())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .isPast(Boolean.TRUE.equals(trip.getIsPast()))
                .representativeImageUrl(resolveRepresentativeImage(trip))
                .emotionName(emotionInfo.emotionName())
                .emotionColor(emotionInfo.emotionColor())
                .visitedCountries(collectVisitedCountries(trip))
                .build();
    }

    /**
     * 여행별 방문한 국가 목록을 수집(활성 일기에서 국가 정보 추출, 중복 제거, VisitedCountryInfoDto로 변환)
     */
    private List<VisitedCountryInfoDto> collectVisitedCountries(Trip trip) {
        List<Diary> diaries = filterActiveDiaries(trip.getDiaries());
        if (diaries == null || diaries.isEmpty()) {
            return List.of();
        }

        // 일기 목록에서 국가 정보 추출 및 중복 제거 (국가 코드 기준)
        Map<String, Country> uniqueCountries = diaries.stream()
                .map(Diary::getCountry)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Country::getCountryCode,
                        country -> country,
                        (existing, duplicate) -> existing, // 중복 시 기존 값 유지
                        LinkedHashMap::new // 순서 보장
                ));

        return uniqueCountries.values().stream()
                .map(this::buildVisitedCountryInfo)
                .toList();
    }

    /**
     * 국가 정보를 VisitedCountryInfoDto로 변환
     */
    private VisitedCountryInfoDto buildVisitedCountryInfo(Country country) {
        return VisitedCountryInfoDto.builder()
                .countryCode(country.getCountryCode())
                .countryName(country.getCountryName())
                .emoji(country.getEmoji())
                .emotionName(null) 
                .emotionColor(null) 
                .build();
    }

    /**
     * 여행의 가장 최근 일기에서 감정 정보를 추출(일기 없거나 감정 정보가 없으면 기본 감정 반환)
     */
    private TripEmotionInfo resolveTripEmotion(Trip trip) {
        List<Diary> diaries = filterActiveDiaries(trip.getDiaries());
        if (diaries == null || diaries.isEmpty()) {
            return getDefaultEmotionInfo();
        }

        // createdAt 기준으로 가장 최근 일기 찾기
        Optional<Diary> mostRecentDiary = diaries.stream()
                .max(Comparator.comparing(Diary::getCreatedAt));

        if (mostRecentDiary.isEmpty() || mostRecentDiary.get().getEmotion() == null) {
            return getDefaultEmotionInfo();
        }

        Emotion emotion = mostRecentDiary.get().getEmotion();
        return new TripEmotionInfo(emotion.getName(), emotion.getColorCode());
    }

    /**
     * 기본 감정 정보를 반환(일기 없거나 감정 정보가 없을 때 사용)
     */
    private TripEmotionInfo getDefaultEmotionInfo() {
        Emotion defaultEmotion = emotionRepository.findByName("기본");
        if (defaultEmotion != null) {
            return new TripEmotionInfo(defaultEmotion.getName(), defaultEmotion.getColorCode());
        }
        // 기본 감정이 없으면 하드코딩된 기본값 반환
        return new TripEmotionInfo("기본", "#EEEEEE");
    }

    //여행의 대표 이미지 URL 결정
    private String resolveRepresentativeImage(Trip trip) {
        // 여행에 직접 설정된 대표 이미지가 있으면 우선 사용
        String representativeImageUrl = trip.getRepresentativeImageUrl();
        if (representativeImageUrl != null && !representativeImageUrl.trim().isEmpty()) {
            return representativeImageUrl;
        }

        // 일기 이미지에서 대표 이미지 찾기
        List<Diary> diaries = filterActiveDiaries(trip.getDiaries());
        if (diaries == null || diaries.isEmpty()) {
            return null;
        }

        // 생성일 기준 오름차순으로 정렬하여 가장 오래된 일기부터 대표 이미지 찾기
        return diaries.stream()
                .sorted(Comparator.comparing(Diary::getCreatedAt))
                .map(this::findRepresentativeImageUrl)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
    }

    /**
     * 일기에서 대표 이미지 URL 찾기
     * 
     * @param diary 일기 엔티티
     * @return 대표 이미지 URL (없으면 empty)
     * 
     * 일기 이미지 중 isRepresentative가 true인 이미지를
     * imageOrder 기준 오름차순으로 정렬하여 첫 번째 이미지 반환
     */
    private Optional<String> findRepresentativeImageUrl(Diary diary) {
        return diary.getDiaryImages().stream()
                .sorted(Comparator.comparingInt(DiaryImage::getImageOrder))
                .filter(DiaryImage::isRepresentative)
                .map(DiaryImage::getImageUrl)
                .findFirst();
    }

    //활성 일기만 필터링
    private List<Diary> filterActiveDiaries(List<Diary> diaries) {
        if (diaries == null || diaries.isEmpty()) {
            return List.of();
        }
        return diaries.stream()
                .filter(this::isActiveDiary)
                .toList();
    }

    //활성 여행 여부 확인
    private boolean isActiveTrip(Trip trip) {
        return !Boolean.TRUE.equals(trip.getIsStored());
    }

    //활성 일기 여부 확인
    private boolean isActiveDiary(Diary diary) {
        return !Boolean.TRUE.equals(diary.getIsStored());
    }

    //사용자 인증 확인
    private void ensureAuthenticated(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }

    //여행의 감정 정보를 담는 레코드
    private record TripEmotionInfo(String emotionName, String emotionColor) {
    }
}

