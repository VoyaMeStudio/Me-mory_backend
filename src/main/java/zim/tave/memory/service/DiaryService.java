package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.CreateDiaryRequest;
import zim.tave.memory.dto.TripRepresentativeImageDto;
import zim.tave.memory.dto.UpdateDiaryOptionalFieldsRequest;
import zim.tave.memory.repository.DiaryRepository;
import zim.tave.memory.repository.TripRepository;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.repository.EmotionRepository;
import zim.tave.memory.repository.WeatherRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final TripRepository tripRepository;
    private final EmotionRepository emotionRepository;
    private final WeatherRepository weatherRepository;
    private final CountryService countryService;
    private final VisitedCountryService visitedCountryService;

    // 이미지 검증&저장
    private void validateAndAttachImages(Diary diary, List<CreateDiaryRequest.DiaryImageInfo> images) {
        if (images == null || images.size() != 2) {
            throw new IllegalArgumentException("이미지는 반드시 2장이어야 합니다. (FRONT/BACK)");
        }

        boolean hasFront = images.stream().anyMatch(img -> img.getCameraType() == DiaryImage.CameraType.FRONT);
        boolean hasBack = images.stream().anyMatch(img -> img.getCameraType() == DiaryImage.CameraType.BACK);

        if (!hasFront || !hasBack) {
            throw new IllegalArgumentException("FRONT/BACK 카메라 사진이 모두 필요합니다.");
        }

        long representativeCount = images.stream().filter(CreateDiaryRequest.DiaryImageInfo::isRepresentative).count();
        if (representativeCount != 1) {
            throw new IllegalArgumentException("대표 이미지는 정확히 1장이어야 합니다.");
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
    public Diary createDiary(CreateDiaryRequest request) {

        // 사용자 검증
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        //여행 검증
        Trip trip = tripRepository.findOne(request.getTripId());
        if (trip == null) {
            throw new IllegalArgumentException("여행을 찾을 수 없습니다. ID=" + request.getTripId());
        }

        //국가 검증
        Country country = countryService.findByCode(request.getCountryCode());
        if (country == null) {
            throw new IllegalArgumentException("유효하지 않은 국가 코드입니다. CODE=" + request.getCountryCode());
        }

        // 감정 검증 (기본 = 1)
        Emotion emotion = emotionRepository.findById(
                        Optional.ofNullable(request.getEmotionId()).orElse(1L))
                .orElseThrow(() -> new IllegalArgumentException("감정을 찾을 수 없습니다."));

        //날씨 검증
        Weather weather = null;
        if (request.getWeatherId() != null) {
            weather = weatherRepository.findById(request.getWeatherId())
                    .orElseThrow(() -> new IllegalArgumentException("날씨를 찾을 수 없습니다."));
        }

        // Diary 생성
        Diary diary = Diary.createDiary(user, trip, country,
                request.getCity(), request.getDateTime(), request.getContent());

        // 이미지 검증 및 저장
        validateAndAttachImages(diary, request.getImages());

        //선택 필드 설정
        diary.setOptionalFields(request.getDetailedLocation(), request.getAudioUrl(), emotion, weather);
        diaryRepository.save(diary);

        trip.updateEndDate(diary.getCreatedAt().toLocalDate());

        // VisitedCountry 등록
        try {
            visitedCountryService.registerVisitedCountry(user.getId(), country.getCountryCode(), emotion.getId());
        } catch (Exception e) {
            System.err.println("VisitedCountry 등록 실패: " + e.getMessage());
        }

        return diary;
    }

    @Transactional
    public void updateDiaryOptionalFields(Long diaryId, UpdateDiaryOptionalFieldsRequest request) {
        Diary diary = diaryRepository.findByIdOrThrow(diaryId);
        Emotion emotion = null;
        if (request.getEmotionId() != null) {
            emotion = emotionRepository.findById(request.getEmotionId())
                    .orElseThrow(() -> new IllegalArgumentException("감정을 찾을 수 없습니다."));
        }
        Weather weather = null;
        if (request.getWeatherId() != null) {
            weather = weatherRepository.findById(request.getWeatherId())
                    .orElseThrow(() -> new IllegalArgumentException("날씨를 찾을 수 없습니다."));
        }
        diary.setOptionalFields(request.getDetailedLocation(), request.getAudioUrl(), emotion, weather);
    }

    @Transactional
    public void updateRepresentativeImage(Long diaryId, Long imageId) {
        Diary diary = diaryRepository.findByIdOrThrow(diaryId);
        diary.getDiaryImages().forEach(img -> img.setRepresentative(false));

        DiaryImage selected = diary.getDiaryImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. ID=" + imageId));

        selected.setRepresentative(true);
    }

    public Diary findOne(Long diaryId) {
        return diaryRepository.findByIdOrThrow(diaryId);
    }

    public List<Diary> findByTripId(Long tripId) {
        return diaryRepository.findByTripId(tripId);
    }

    public List<Diary> findByUserId(Long userId) {
        return diaryRepository.findByUserId(userId);
    }

    // 여행별 대표사진 조회
    public List<TripRepresentativeImageDto> getRepresentativeImagesByTripId(Long tripId) {
        List<Diary> diaries = diaryRepository.findByTripId(tripId);

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
    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findByIdOrThrow(diaryId);
        Long tripId = diary.getTrip().getId();
        diaryRepository.delete(diary);

        // 다이어리 삭제 후 Trip의 종료 날짜를 업데이트
        Trip trip = tripRepository.findOne(tripId);
        List<Diary> remaining = diaryRepository.findByTripId(tripId);

        if (remaining.isEmpty()) {
            trip.setEndDate(null);
        } else {
            LocalDate latest = remaining.stream()
                    .map(d -> d.getCreatedAt().toLocalDate())
                    .max(LocalDate::compareTo)
                    .orElse(null);
            trip.setEndDate(latest);
        }
    }

    // DiaryImage ID로 DiaryImage 찾기
    public DiaryImage findDiaryImageById(Long imageId) {
        List<Diary> allDiaries = diaryRepository.findAll();
        return allDiaries.stream()
                .flatMap(diary -> diary.getDiaryImages().stream())
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. ID: " + imageId));
    }
}
