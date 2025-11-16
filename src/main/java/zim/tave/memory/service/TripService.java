package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.CreateTripRequest;
import zim.tave.memory.dto.TripResponseDto;
import zim.tave.memory.dto.UpdateTripRequest;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.DiaryRepository;
import zim.tave.memory.repository.TripRepository;
import zim.tave.memory.repository.TripThemeRepository;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripThemeRepository tripThemeRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    @Transactional
    public TripResponseDto createTrip(CreateTripRequest request, Long userId) {
        // 테마 처리: 테마가 선택되지 않으면 기본 테마(id=1)를 사용
        Long themeId = request.getThemeId();
        if (themeId == null) {
            themeId = 1L; // 기본 테마 ID
        }

        TripTheme theme = tripThemeRepository.findById(themeId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        Trip trip = Trip.createTrip(user, request.getTripName(), request.getDescription(), theme);
        Trip saved = tripRepository.save(trip);
        return buildTripResponseDto(saved);
    }

    @Transactional
    public void saveTrip(Trip trip) {
        tripRepository.save(trip);
    }

    @Transactional
    public void updateTrip(Long tripId, UpdateTripRequest request, Long userId) {
        Trip findTrip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        if (!findTrip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }

        if (request.getTripName() != null) {
            findTrip.setTripName(request.getTripName());
        }

        if (request.getDescription() != null) {
            findTrip.setDescription(request.getDescription());
        }

        if (request.getThemeId() != null) {
            TripTheme theme = tripThemeRepository.findById(request.getThemeId())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
            findTrip.setTripTheme(theme);
        }

        if (request.getRepresentativeImageUrl() != null) {
            findTrip.setRepresentativeImageUrl(request.getRepresentativeImageUrl());
        }

        if (request.getStartDate() != null) {
            findTrip.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            findTrip.setEndDate(request.getEndDate());
        }
    }

    public List<Trip> findAll() {
        return tripRepository.findAllWithDiaries();
    }

    public Trip findOne(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));
    }

    public List<Trip> findByUserId(Long userId) {
        return tripRepository.findByUserIdWithDiaries(userId);
    }

    // 여행의 마지막 다이어리 날짜를 기준으로 종료 날짜 업데이트
    @Transactional
    public void updateTripEndDate(Long tripId) {
        tripRepository.updateTripEndDate(tripId);
    }

    // 여행의 마지막 다이어리 날짜 조회
    public LocalDate getLastDiaryDateByTripId(Long tripId) {
        return tripRepository.findLastDiaryDateByTripId(tripId);
    }

    // 여행의 대표 사진 설정
    @Transactional
    public void updateTripRepresentativeImage(Long tripId, Long imageId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        // 해당 이미지가 이 여행에 속하는 다이어리의 대표 사진인지 검증
        DiaryImage diaryImage = findDiaryImageById(imageId);

        if (!diaryImage.getDiary().getTrip().getId().equals(tripId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (!diaryImage.isRepresentative()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        trip.setRepresentativeImageUrl(diaryImage.getImageUrl());
    }

    // DiaryImage ID로 DiaryImage 찾기
    private DiaryImage findDiaryImageById(Long imageId) {
        List<Diary> allDiaries = diaryRepository.findAll();
        return allDiaries.stream()
                .flatMap(diary -> diary.getDiaryImages().stream())
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
    }

    public List<TripResponseDto> getTripsByUserId(Long userId) {
        List<Trip> trips = tripRepository.findByUserIdWithDiaries(userId);
        return trips.stream().map(this::buildTripResponseDto).toList();
    }

    public TripResponseDto getTripDto(Long tripId) {
        Trip trip = findOne(tripId);
        return buildTripResponseDto(trip);
    }

    private TripResponseDto buildTripResponseDto(Trip trip) {
        String representativeImageUrl = trip.getRepresentativeImageUrl();
        if ((representativeImageUrl == null || representativeImageUrl.trim().isEmpty())
                && !trip.getDiaries().isEmpty()) {
            representativeImageUrl = trip.getDiaries().stream()
                    .sorted((d1, d2) -> d1.getCreatedAt().compareTo(d2.getCreatedAt()))
                    .findFirst()
                    .flatMap(diary -> diary.getDiaryImages().stream()
                            .filter(DiaryImage::isRepresentative)
                            .findFirst()
                            .map(DiaryImage::getImageUrl))
                    .orElse(null);
        }

        return TripResponseDto.builder()
                .id(trip.getId())
                .tripName(trip.getTripName())
                .description(trip.getDescription())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .representativeImageUrl(representativeImageUrl)
                .userId(trip.getUser() != null ? trip.getUser().getId() : null)
                .userKakaoId(trip.getUser() != null ? trip.getUser().getKakaoId() : null)
                .userKoreanName(trip.getUser() != null ? trip.getUser().getKoreanName() : null)
                .themeId(trip.getTripTheme() != null ? trip.getTripTheme().getId() : null)
                .themeName(trip.getTripTheme() != null ? trip.getTripTheme().getThemeName() : null)
                .themeSampleImageUrl(trip.getTripTheme() != null ? trip.getTripTheme().getSampleImageUrl() : null)
                .themeCardImageUrl(trip.getTripTheme() != null ? trip.getTripTheme().getCardImageUrl() : null)
                .diaryCount(trip.getDiaries() != null ? trip.getDiaries().size() : 0)
                .build();
    }
}
