package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.request.CreatePastTripRequest;
import zim.tave.memory.dto.request.CreateTripRequest;
import zim.tave.memory.dto.response.TripResponseDto;
import zim.tave.memory.dto.request.UpdateTripRequest;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.*;

import java.time.LocalDate;
import java.time.ZoneId;
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
    private final DiaryRepository diaryRepository;

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

        User user = loadUser(userId);


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

        User user = loadUser(userId);

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
        ensureAuthenticated(userId);
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
        
        // 날짜 검증: 하나의 날짜만 수정되더라도 기존 날짜와 비교 검증
        LocalDate existingStartDate = findTrip.getStartDate();
        LocalDate existingEndDate = findTrip.getEndDate();
        
        // DB 제약에 의해 null이 아니어야 하지만, 데이터 무결성 검증
        if (existingStartDate == null || existingEndDate == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        
        LocalDate newStartDate = request.getStartDate() != null ? request.getStartDate() : existingStartDate;
        LocalDate newEndDate = request.getEndDate() != null ? request.getEndDate() : existingEndDate;
        
        if (newStartDate.isAfter(newEndDate)) {
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
        ensureAuthenticated(userId);
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
        ensureAuthenticated(userId);
        Trip trip = findOne(tripId);
        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }
    }

    public TripResponseDto getTripDtoWithOwnershipCheck(Long tripId, Long userId) {
        ensureAuthenticated(userId);
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
        ensureAuthenticated(userId);
        List<Trip> trips = tripRepository.findByUserIdWithDiaries(userId);
        return trips.stream().map(this::buildTripResponseDto).toList();
    }

    public TripResponseDto getTripDto(Long tripId) {
        Trip trip = findOne(tripId);
        return buildTripResponseDto(trip);
    }

    @Transactional
    public void storeTrip(Long tripId, Long userId, boolean isStored) {
        ensureAuthenticated(userId);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }

        trip.setIsStored(isStored);
        List<Diary> diaries = diaryRepository.findByTripId(tripId);
        diaries.forEach(diary -> diary.setIsStored(isStored));
    }

    @Transactional
    public void deleteTrip(Long tripId, Long userId) {
        ensureAuthenticated(userId);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }

        tripRepository.delete(trip);
    }

    /**
     * 과거 여행인지 확인하는 검증 로직
     *
     * @param trip 여행 엔티티
     * @throws CustomException 과거 여행이 아니면 NOT_PAST_TRIP 예외 발생
     */
    private void validateIsPastTrip(Trip trip) {
        if (!Boolean.TRUE.equals(trip.getIsPast())) {
            throw new CustomException(ErrorCode.NOT_PAST_TRIP);
        }
    }

    /**
     * 과거 여행만 수정하는 메서드 (타임라인 뷰용)
     *
     * @param tripId 여행 ID
     * @param request 수정 요청
     * @param userId 사용자 ID
     * @throws CustomException 과거 여행이 아니면 NOT_PAST_TRIP 예외 발생
     */
    @Transactional
    public void updatePastTrip(Long tripId, UpdateTripRequest request, Long userId) {
        ensureAuthenticated(userId);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }

        validateIsPastTrip(trip);

        // 입력 검증
        if (request.getTripName() != null && request.getTripName().trim().length() > 14) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.getDescription() != null && request.getDescription().trim().length() > 56) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        
        // 날짜 검증: 하나의 날짜만 수정되더라도 기존 날짜와 비교 검증
        LocalDate existingStartDate = trip.getStartDate();
        LocalDate existingEndDate = trip.getEndDate();
        
        // DB 제약에 의해 null이 아니어야 하지만, 데이터 무결성 검증
        if (existingStartDate == null || existingEndDate == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        
        LocalDate newStartDate = request.getStartDate() != null ? request.getStartDate() : existingStartDate;
        LocalDate newEndDate = request.getEndDate() != null ? request.getEndDate() : existingEndDate;
        
        if (newStartDate.isAfter(newEndDate)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        if (request.getTripName() != null) {
            trip.setTripName(request.getTripName());
        }

        if (request.getDescription() != null) {
            trip.setDescription(request.getDescription());
        }

        if (request.getThemeId() != null) {
            TripTheme theme = tripThemeRepository.findById(request.getThemeId())
                    .orElseThrow(() -> new CustomException(ErrorCode.TRIP_THEME_NOT_FOUND));
            trip.setTripTheme(theme);
        }

        if (request.getRepresentativeImageUrl() != null) {
            trip.setRepresentativeImageUrl(request.getRepresentativeImageUrl());
        }

        if (request.getStartDate() != null) {
            trip.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            trip.setEndDate(request.getEndDate());
        }
    }

    /**
     * 과거 여행만 보관하는 메서드 (타임라인 뷰용)
     *
     * @param tripId 여행 ID
     * @param userId 사용자 ID
     * @param isStored 보관 여부
     * @throws CustomException 과거 여행이 아니면 NOT_PAST_TRIP 예외 발생
     */
    @Transactional
    public void storePastTrip(Long tripId, Long userId, boolean isStored) {
        ensureAuthenticated(userId);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }

        validateIsPastTrip(trip);
        storeTrip(tripId, userId, isStored);
    }

    /**
     * 과거 여행만 삭제하는 메서드 (타임라인 뷰용)
     *
     * @param tripId 여행 ID
     * @param userId 사용자 ID
     * @throws CustomException 과거 여행이 아니면 NOT_PAST_TRIP 예외 발생
     */
    @Transactional
    public void deletePastTrip(Long tripId, Long userId) {
        ensureAuthenticated(userId);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }

        validateIsPastTrip(trip);
        tripRepository.delete(trip);
    }

    private void validateTripPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELDS);
        }
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }
// 인증 여부 검증
    private void ensureAuthenticated(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }
// 사용자 조회
    private User loadUser(Long userId) {
        ensureAuthenticated(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
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

    // (알림 전송용) 현재 진행중인 여행 확인
    public Trip findCurrentOngoingTrip(Long userId) {
        ensureAuthenticated(userId);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return tripRepository.findOngoingTripByUserId(userId, today)
                .orElse(null);
    }
}
