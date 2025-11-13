package zim.tave.memory.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.Emotion;
import zim.tave.memory.domain.User;
import zim.tave.memory.domain.VisitedCountry;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.CountryRepository;
import zim.tave.memory.repository.EmotionRepository;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.repository.VisitedCountryRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VisitedCountryService {

    private final VisitedCountryRepository visitedCountryRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final EmotionRepository emotionRepository;

    //사용자의 모든 방문 국가 목록 조회 (감정 정보 포함)
    public List<VisitedCountry> getVisitedCountries(Long userId) {
        return visitedCountryRepository.findByUserId(userId);
    }

    //사용자가 이미 특정 국가를 방문했는지 여부
    public boolean alreadyVisited(Long userId, String countryCode) {
        return visitedCountryRepository.existsByUserIdAndCountryCode(userId, countryCode);
    }

    //방문 국가 등록 (기존 기록이 있으면 감정과 색상 업데이트)
    public VisitedCountry registerVisitedCountry(Long userId, String countryCode, Long emotionId) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_COUNTRY_CODE);
        }
        String normalizedCode = countryCode.trim().toUpperCase();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Country country = countryRepository.findByCode(normalizedCode);
        if (country == null) {
            throw new CustomException(ErrorCode.COUNTRY_NOT_FOUND);
        }
        // 감정이 null이면 '설렘'으로 대체
        Emotion emotion;
        if (emotionId == null) {
            emotion = emotionRepository.findByName("설렘");
            if (emotion == null) {
                throw new CustomException(ErrorCode.DEFAULT_EMOTION_NOT_CONFIGURED);
            }
        } else {
            emotion = emotionRepository.findById(emotionId)
                    .orElseThrow(() -> new CustomException(ErrorCode.EMOTION_NOT_FOUND));
        }

        // 기존 기록이 있는지 확인
        var existingVisitedCountry = visitedCountryRepository.findByUserIdAndCountryCode(userId, normalizedCode);
        
        if (existingVisitedCountry.isPresent()) {
            // 기존 기록이 있으면 감정과 색상 업데이트 후 반환
            VisitedCountry visited = existingVisitedCountry.get();
            visited.setEmotion(emotion);
            visited.setColor(emotion.getColorCode());
            visitedCountryRepository.save(visited);
            return visited;
        }

        // 새로운 기록 생성
        VisitedCountry visited = new VisitedCountry();
        visited.setUser(user);
        visited.setCountry(country);
        visited.setEmotion(emotion);
        visited.setColor(emotion.getColorCode());
        visitedCountryRepository.save(visited);
        return visited;
    }

    //특정 사용자의 특정 방문 국가 삭제
    public void deleteVisitedCountry(Long userId, String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_COUNTRY_CODE);
        }
        String normalizedCode = countryCode.trim().toUpperCase();
        VisitedCountry visitedCountry = visitedCountryRepository.findByUserIdAndCountryCode(userId, normalizedCode)
                .orElseThrow(() -> new CustomException(ErrorCode.VISITED_COUNTRY_NOT_FOUND));
        visitedCountryRepository.delete(visitedCountry);
    }
}
