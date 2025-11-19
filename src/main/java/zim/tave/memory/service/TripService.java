package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.DiaryImage;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.domain.TripTheme;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.CreatePastTripRequest;
import zim.tave.memory.dto.CreateTripRequest;
import zim.tave.memory.dto.TripResponseDto;
import zim.tave.memory.dto.UpdateTripRequest;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.DiaryImageRepository;
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
	private final DiaryImageRepository diaryImageRepository;
    private final UserRepository userRepository;
    private final VisitedCountryService visitedCountryService;

    @Transactional
    public TripResponseDto createTrip(CreateTripRequest request, Long userId) {
        // 입력 검증
        if (request.getTripName() == null || request.getTripName().trim().isEmpty()) {
            throw new CustomException(ErrorCode.TRIP_NAME_REQUIRED);
        }
        if (request.getTripName().trim().length() > 14) {
            throw new CustomException(ErrorCode.TRIP_NAME_TOO_LONG);
        }
        if (request.getDescription() != null && request.getDescription().trim().length() > 56) {
            throw new CustomException(ErrorCode.TRIP_DESCRIPTION_TOO_LONG);
        }
        validateTripPeriod(request.getStartDate(), request.getEndDate());

        // 테마 처리: 테마가 선택되지 않으면 기본 테마(id=1)를 사용
        Long themeId = request.getThemeId();
        if (themeId == null) {
            themeId = 1L; // 기본 테마 ID
        }

        TripTheme theme = tripThemeRepository.findById(themeId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_THEME_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        Trip trip = Trip.createTrip(user, request.getTripName(), request.getDescription(), theme);
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setIsPast(false);
        trip.setIsStored(false);
        Trip saved = tripRepository.save(trip);
        return buildTripResponseDto(saved);
    }

    @Transactional
    public TripResponseDto createPastTrip(CreatePastTripRequest request, Long userId) {
        if (request.getTripName() == null || request.getTripName().trim().isEmpty()) {
            throw new CustomException(ErrorCode.TRIP_NAME_REQUIRED);
        }
        if (request.getTripName().trim().length() > 14) {
            throw new CustomException(ErrorCode.TRIP_NAME_TOO_LONG);
        }
        if (request.getDescription() != null && request.getDescription().trim().length() > 56) {
            throw new CustomException(ErrorCode.TRIP_DESCRIPTION_TOO_LONG);
        }
        validateTripPeriod(request.getStartDate(), request.getEndDate());
        if (request.getCountryCodes() == null || request.getCountryCodes().isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELDS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        TripTheme theme = tripThemeRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_THEME_NOT_FOUND));

        Trip trip = Trip.createTrip(user, request.getTripName(), request.getDescription(), theme);
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setIsPast(true);
        trip.setIsStored(false);

        Trip saved = tripRepository.save(trip);

        request.getCountryCodes().forEach(countryCode ->
                visitedCountryService.registerVisitedCountry(userId, countryCode, request.getEmotionId()));

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

        // 입력 검증
        if (request.getTripName() != null && request.getTripName().trim().length() > 14) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.getDescription() != null && request.getDescription().trim().length() > 56) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().isAfter(request.getEndDate())) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        if (request.getTripName() != null) {
            findTrip.setTripName(request.getTripName());
        }

        if (request.getDescription() != null) {
            findTrip.setDescription(request.getDescription());
        }

        if (request.getThemeId() != null) {
            TripTheme theme = tripThemeRepository.findById(request.getThemeId())
                    .orElseThrow(() -> new CustomException(ErrorCode.TRIP_THEME_NOT_FOUND));
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
            throw new CustomException(ErrorCode.IMAGE_TRIP_MISMATCH);
        }

        if (!diaryImage.isRepresentative()) {
            throw new CustomException(ErrorCode.IMAGE_NOT_REPRESENTATIVE);
        }

        trip.setRepresentativeImageUrl(diaryImage.getImageUrl());
    }

    // 컨트롤러 단순화용: 소유권 검증
    public void validateOwnership(Long tripId, Long userId) {
        Trip trip = findOne(tripId);
        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }
    }

    public TripResponseDto getTripDtoWithOwnershipCheck(Long tripId, Long userId) {
        Trip trip = findOne(tripId);
        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }
        return buildTripResponseDto(trip);
    }

    @Transactional
    public void updateTripRepresentativeImageWithOwnershipCheck(Long tripId, Long imageId, Long userId) {
        if (imageId == null) {
            throw new CustomException(ErrorCode.IMAGE_ID_REQUIRED);
        }
        validateOwnership(tripId, userId);
        updateTripRepresentativeImage(tripId, imageId);
    }

    // DiaryImage ID로 DiaryImage 찾기
    private DiaryImage findDiaryImageById(Long imageId) {
		return diaryImageRepository.findById(imageId)
				.orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
    }

    public List<TripResponseDto> getTripsByUserId(Long userId) {
        List<Trip> trips = tripRepository.findByUserIdWithDiaries(userId);
        return trips.stream().map(this::buildTripResponseDto).toList();
    }

    public TripResponseDto getTripDto(Long tripId) {
        Trip trip = findOne(tripId);
        return buildTripResponseDto(trip);
    }

    @Transactional
    public void storeTrip(Long tripId, Long userId, boolean isStored) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }

        trip.setIsStored(isStored);
    }

    private void validateTripPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELDS);
        }
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
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
