package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.Emotion;
import zim.tave.memory.domain.User;
import zim.tave.memory.domain.VisitedCountry;
import zim.tave.memory.dto.response.VisitedCountryListResponseDto;
import zim.tave.memory.dto.response.VisitedCountryResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.EmotionRepository;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.repository.VisitedCountryRepository;
import zim.tave.memory.util.CountryValidator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class VisitedCountryService {

    private final VisitedCountryRepository visitedCountryRepository;
    private final CountryService countryService;
    private final UserRepository userRepository;
    private final EmotionRepository emotionRepository;

    //사용자의 모든 방문 국가 목록 조회 (감정 정보 포함)
    @Transactional(readOnly = true)
    public List<VisitedCountryResponseDto> getVisitedCountries(Long userId) {
        List<VisitedCountry> visitedCountries = visitedCountryRepository.findByUserIdWithDetails(userId);
        return visitedCountries.stream()
                .map(VisitedCountryResponseDto::from)
                .toList();
    }

    //사용자가 이미 특정 국가를 방문했는지 여부
    public boolean alreadyVisited(Long userId, String countryCode) {
        return visitedCountryRepository.existsByUserIdAndCountryCode(userId, countryCode);
    }

    //방문 국가 등록 (기존 기록이 있으면 감정과 색상 업데이트)
    public VisitedCountryResponseDto registerVisitedCountry(Long userId, String countryCode, Long emotionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Country country = countryService.findByCode(countryCode);
        String normalizedCode = country.getCountryCode();
        // 감정이 null이면 '설렘'으로 대체, 없으면 '기본'으로 대체
        Emotion emotion;
        if (emotionId == null) {
            emotion = emotionRepository.findByName("설렘");
            if (emotion == null) {
                // 설렘도 없으면 기본 감정 사용
                emotion = emotionRepository.findByName("기본");
                if (emotion == null) {
                    // 기본 감정도 없으면 첫 번째 감정 사용 (데이터 초기화가 되어 있다고 가정)
                    emotion = emotionRepository.findAllByOrderById().stream()
                            .findFirst()
                            .orElseThrow(() -> new CustomException(ErrorCode.DEFAULT_EMOTION_NOT_CONFIGURED));
                }
            }
        } else {
            emotion = emotionRepository.findById(emotionId)
                    .orElseThrow(() -> new CustomException(ErrorCode.EMOTION_NOT_FOUND));
        }

        // 기존 기록이 있는지 확인
        var existingVisitedCountry = visitedCountryRepository.findByUserIdAndCountryCodeWithDetails(userId, normalizedCode);

        if (existingVisitedCountry.isPresent()) {
            // 기존 기록이 있으면 감정과 색상 업데이트 후 반환
            VisitedCountry visited = existingVisitedCountry.get();
            visited.setEmotion(emotion);
            visited.setColor(emotion.getColorCode());
            visitedCountryRepository.save(visited);
            return VisitedCountryResponseDto.from(visited);
        }

        // 새로운 기록 생성
        VisitedCountry visited = new VisitedCountry();
        visited.setUser(user);
        visited.setCountry(country);
        visited.setEmotion(emotion);
        visited.setColor(emotion.getColorCode());
        visitedCountryRepository.save(visited);
        return VisitedCountryResponseDto.from(visited);
    }

    //특정 사용자의 특정 방문 국가 삭제
    public void deleteVisitedCountry(Long userId, String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_COUNTRY_CODE);
        }
        String normalizedCode = CountryValidator.normalizeCountryCode(countryCode);
        VisitedCountry visitedCountry = visitedCountryRepository.findByUserIdAndCountryCodeWithDetails(userId, normalizedCode)
                .orElseThrow(() -> new CustomException(ErrorCode.VISITED_COUNTRY_NOT_FOUND));
        visitedCountryRepository.delete(visitedCountry);
    }

    @Transactional(readOnly = true)
    public Map<String, VisitedCountry> getVisitedCountryMap(Long userId) {
        List<VisitedCountry> visitedCountries = visitedCountryRepository.findByUserIdWithDetails(userId);
        return visitedCountries.stream()
                .collect(Collectors.toMap(
                        vc -> vc.getCountry().getCountryCode(),
                        vc -> vc,
                        (existing, duplicate) -> existing,
                        LinkedHashMap::new
                ));
    }

    // ✅ 방문 국가 목록 조회
    // 마이페이지 - 방문 국가 목록 조회 (중복X)
    public VisitedCountryListResponseDto getVisitedCountryList(Long userId) {
        // 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 방문 국가 조회 (중복 제거)
        List<VisitedCountry> visitedCountries = visitedCountryRepository.findByUserIdWithDetails(userId);

        // 방문 국가 없으면 빈 리스트 리턴
        if (visitedCountries.isEmpty()) {
            return VisitedCountryListResponseDto.empty();
        }

        //ISO 중복 제거
        Map<String, Country> uniqueCountries = visitedCountries.stream()
                .collect(Collectors.toMap(
                        vc -> vc.getCountry().getCountryCode(),
                        VisitedCountry::getCountry,
                        (existing, duplicate) -> existing  // 중복 제거
                ));

        Long countryCount = user.getVisitedCountryCount();

        return VisitedCountryListResponseDto.from(countryCount, uniqueCountries.values());
    }
}
