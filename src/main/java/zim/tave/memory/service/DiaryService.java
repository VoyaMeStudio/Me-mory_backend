package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.request.CreateDiaryRequest;
import zim.tave.memory.dto.response.DiaryResponseDto;
import zim.tave.memory.dto.TripRepresentativeImageDto;
import zim.tave.memory.dto.request.UpdateDiaryOptionalFieldsRequest;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final TripRepository tripRepository;
    private final EmotionRepository emotionRepository;
    private final WeatherRepository weatherRepository;
	private final DiaryImageRepository diaryImageRepository;
    private final CountryService countryService;
    private final VisitedCountryService visitedCountryService;
    private final TripCountryService tripCountryService;

    // 이미지 검증&저장
    private void validateAndAttachImages(Diary diary, List<CreateDiaryRequest.DiaryImageInfo> images) {
        if (images == null || images.size() != 2) {
			throw new CustomException(ErrorCode.IMAGE_COUNT_INVALID);
        }

        boolean hasFront = images.stream().anyMatch(img -> img.getCameraType() == DiaryImage.CameraType.FRONT);
        boolean hasBack = images.stream().anyMatch(img -> img.getCameraType() == DiaryImage.CameraType.BACK);

        if (!hasFront || !hasBack) {
			throw new CustomException(ErrorCode.CAMERA_TYPES_REQUIRED);
        }

        long representativeCount = images.stream().filter(CreateDiaryRequest.DiaryImageInfo::isRepresentative).count();
        if (representativeCount != 1) {
			throw new CustomException(ErrorCode.REPRESENTATIVE_IMAGE_REQUIRED);
        }

        for (int i = 0; i < images.size(); i++) {
            CreateDiaryRequest.DiaryImageInfo imageInfo = images.get(i);
            DiaryImage image = new DiaryImage();
            image.setDiary(diary);
            image.setImageUrl(imageInfo.getImageUrl());
            image.setCameraType(imageInfo.getCameraType());
            image.setImageOrder(i + 1);
            image.setRepresentative(imageInfo.isRepresentative());
            diary.addDiaryImage(image);
        }
    }

    @Transactional
	public DiaryResponseDto createDiary(CreateDiaryRequest request) {

        // 사용자 검증
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //여행 검증
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));
        // 과거 여행인 경우 다이어리 추가 불가
        if (Boolean.TRUE.equals(trip.getIsPast())) {
            throw new CustomException(ErrorCode.CANNOT_ADD_DIARY_TO_PAST_TRIP);
        }

        //국가 검증
        Country country = countryService.findByCode(request.getCountryCode());
        if (country == null) throw new CustomException(ErrorCode.INVALID_COUNTRY_CODE);

        // 날짜 및 시간 자동 생성 (UTC 기준)
        OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.UTC);

        // 감정 검증 (기본 = 1)
        Emotion emotion = emotionRepository.findById(
                        Optional.ofNullable(request.getEmotionId()).orElse(1L))
                .orElseThrow(() -> new CustomException(ErrorCode.EMOTION_NOT_FOUND));

        //날씨 검증
        Weather weather = null;
        if (request.getWeatherId() != null) {
            weather = weatherRepository.findById(request.getWeatherId())
                    .orElseThrow(() -> new CustomException(ErrorCode.WEATHER_NOT_FOUND));
        }

        // Diary 생성 (content는 선택 필드이므로 null 허용)
        Diary diary = Diary.createDiary(user, trip, country,
                request.getCity(), dateTime, request.getContent());

        // 이미지 검증 및 저장
        validateAndAttachImages(diary, request.getImages());

        //선택 필드 설정
        diary.setOptionalFields(request.getDetailedLocation(), emotion, weather);
		Diary saved = diaryRepository.save(diary);

		trip.updateEndDate(saved.getCreatedAt().toLocalDate());

        tripCountryService.registerTripCountry(trip, country);

        // VisitedCountry 등록
        try {
            visitedCountryService.registerVisitedCountry(user.getId(), country.getCountryCode(), emotion.getId());
        } catch (Exception e) {
            log.error("VisitedCountry 등록 실패", e);
        }

		return convertToDto(saved);
    }

    @Transactional
    public void updateDiaryOptionalFields(Long userId, Long diaryId, UpdateDiaryOptionalFieldsRequest request) {
		Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        checkDiaryOwnership(userId, diary);

        // 장소명(도시)
        if (request.getCity() != null && !request.getCity().isBlank()) {
            diary.setCity(request.getCity());
        }
        // 상세 위치
        if (request.getDetailedLocation() != null) {
            diary.setDetailedLocation(request.getDetailedLocation());
        }
        // 기록(본문)
        if (request.getContent() != null) {
            diary.setContent(request.getContent());
        }

        Emotion emotion = null;
        if (request.getEmotionId() != null) {
            emotion = emotionRepository.findById(request.getEmotionId())
					.orElseThrow(() -> new CustomException(ErrorCode.EMOTION_NOT_FOUND));
        }
        Weather weather = null;
        if (request.getWeatherId() != null) {
            weather = weatherRepository.findById(request.getWeatherId())
                    .orElseThrow(() -> new CustomException(ErrorCode.WEATHER_NOT_FOUND));
        }
        diary.setOptionalFields(diary.getDetailedLocation(), emotion, weather);
    }

    @Transactional
    public void updateRepresentativeImage(Long diaryId, Long imageId) {
		if (imageId == null) {
			throw new CustomException(ErrorCode.IMAGE_ID_REQUIRED);
		}
		Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
		checkDiaryOwnership(diary.getUser().getId(), diary); // ownership check defensive
        diary.getDiaryImages().forEach(img -> img.setRepresentative(false));

        DiaryImage selected = diaryImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));

        selected.setRepresentative(true);
    }

	public DiaryResponseDto findOne(Long diaryId, Long userId) {
		Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
		checkDiaryOwnership(userId, diary);
		return convertToDto(diary);
    }

	public List<DiaryResponseDto> findByTripId(Long tripId, Long userId) {
		List<Diary> diaries = diaryRepository.findByTrip_Id(tripId);
		if (!diaries.isEmpty() && !diaries.get(0).getUser().getId().equals(userId)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
		return diaries.stream().map(this::convertToDto).collect(Collectors.toList());
    }

	public List<DiaryResponseDto> findByUserId(Long userId) {
		return diaryRepository.findByUser_Id(userId).stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
    }

    // 여행별 대표사진 조회
    public List<TripRepresentativeImageDto> getRepresentativeImagesByTripId(Long tripId) {
		List<Diary> diaries = diaryRepository.findByTrip_Id(tripId);

        return diaries.stream()
                .filter(diary -> diary.getDiaryImages() != null && !diary.getDiaryImages().isEmpty())
                .map(diary -> {
                    // 대표사진 찾기 (대표사진이 없으면 첫 번째 이미지)
                    DiaryImage representativeImage = diary.getDiaryImages().stream()
                            .filter(DiaryImage::isRepresentative)
                            .findFirst()
                            .orElse(diary.getDiaryImages().get(0));

                                    return new TripRepresentativeImageDto(
                        diary.getId(),
                        representativeImage.getId(),
                        representativeImage.getImageUrl(),
                        diary.getCountry().getCountryCode(),
                        diary.getCountry().getCountryName(),
                        diary.getCity(),
                        diary.getContent()
                );
                })
                .collect(Collectors.toList());
    }

	@Transactional
    public void storeDiary(Long diaryId, Long userId, boolean isStored) {
        if (userId == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

        if (!diary.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        diary.setIsStored(isStored);
        syncTripCountryForDiaryVisibility(diary, isStored);
    }

    @Transactional
    public void deleteDiary(Long diaryId, Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }
		Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
		checkDiaryOwnership(userId, diary);
        Long tripId = diary.getTrip().getId();
        removeTripCountryIfLastActiveDiary(diary);
		diaryRepository.delete(diary);

        // 다이어리 삭제 후 Trip의 종료 날짜를 업데이트
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));
		List<Diary> remaining = diaryRepository.findByTrip_Id(tripId);

        if (remaining.isEmpty()) {
            trip.setEndDate(trip.getStartDate());
        } else {
            LocalDate latest = remaining.stream()
                    .map(d -> d.getCreatedAt().toLocalDate())
                    .max(LocalDate::compareTo)
                    .orElse(null);
            trip.setEndDate(latest);
        }
    }

	private void checkDiaryOwnership(Long userId, Diary diary) {
		if (!diary.getUser().getId().equals(userId)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
    }

    private void syncTripCountryForDiaryVisibility(Diary diary, boolean isStored) {
        if (isStored) {
            removeTripCountryIfLastActiveDiary(diary);
            return;
        }
        if (diary.getTrip() != null && diary.getCountry() != null) {
            tripCountryService.registerTripCountry(diary.getTrip(), diary.getCountry());
        }
    }

    private void removeTripCountryIfLastActiveDiary(Diary diary) {
        if (diary.getTrip() == null || diary.getTrip().getId() == null
                || diary.getCountry() == null || diary.getCountry().getCountryCode() == null
                || diary.getId() == null) {
            return;
        }

        boolean hasOtherActiveDiary = diaryRepository.existsOtherActiveDiaryByTripIdAndCountryCode(
                diary.getTrip().getId(),
                diary.getCountry().getCountryCode(),
                diary.getId()
        );
        if (!hasOtherActiveDiary) {
            tripCountryService.deleteTripCountry(diary.getTrip(), diary.getCountry());
        }
    }

	private DiaryResponseDto convertToDto(Diary diary) {
		DiaryResponseDto.DiaryResponseDtoBuilder builder = DiaryResponseDto.builder()
				.id(diary.getId())
				.city(diary.getCity())
				.detailedLocation(diary.getDetailedLocation())
				.dateTime(diary.getDateTime())
				.createdAt(diary.getCreatedAt())
				.content(diary.getContent())
				.tripId(diary.getTrip() != null ? diary.getTrip().getId() : null)
				.tripName(diary.getTrip() != null ? diary.getTrip().getTripName() : null);

		if (diary.getCountry() != null) {
			builder.countryName(diary.getCountry().getCountryName())
					.countryEmoji(diary.getCountry().getEmoji());
		}
		if (diary.getEmotion() != null) {
			builder.emotionColor(diary.getEmotion().getColorCode())
					.emotionName(diary.getEmotion().getName());
		}
		if (diary.getWeather() != null) {
			builder.weather(diary.getWeather().getName())
					.weatherIconUrl(diary.getWeather().getIconUrl());
		}

		List<DiaryResponseDto.DiaryImageDto> images = diary.getDiaryImages().stream()
				.map(img -> DiaryResponseDto.DiaryImageDto.builder()
						.id(img.getId())
						.imageUrl(img.getImageUrl())
						.cameraType(img.getCameraType().name())
						.isRepresentative(img.isRepresentative())
						.imageOrder(img.getImageOrder())
						.build())
				.collect(Collectors.toList());

		builder.images(images);
		return builder.build();
	}
}
