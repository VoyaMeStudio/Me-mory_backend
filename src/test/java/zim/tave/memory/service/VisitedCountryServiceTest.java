package zim.tave.memory.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.Emotion;
import zim.tave.memory.domain.User;
import zim.tave.memory.domain.VisitedCountry;
import zim.tave.memory.repository.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class VisitedCountryServiceTest {

    @Autowired
    private VisitedCountryService visitedCountryService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private EmotionRepository emotionRepository;

    @Test
    void testRegisterVisitedCountry_SuccessAndUpdate() {
        // given: 테스트용 데이터 생성
        User user = createTestUser("testKakao1");
        Country country = createTestCountry("KR1", "대한민국1", "🇰🇷");
        Emotion emotion1 = createTestEmotion("행복1", "#FFD700");
        Emotion emotion2 = createTestEmotion("슬픔1", "#0000FF");
        
        // when: 첫 번째 등록
        VisitedCountry first = visitedCountryService.registerVisitedCountry(user.getId(), country.getCountryCode(), emotion1.getId());
        assertThat(first.getEmotion().getName()).isEqualTo("행복1");

        // 두 번째 등록 (감정 변경)
        VisitedCountry updated = visitedCountryService.registerVisitedCountry(user.getId(), country.getCountryCode(), emotion2.getId());

        List<VisitedCountry> visitedList = visitedCountryService.getVisitedCountries(user.getId());

        // then: 하나의 기록만 존재하고, 두 번째 감정으로 업데이트됨
        assertThat(visitedList).hasSize(1);
        assertThat(first.getEmotion().getName()).isEqualTo("슬픔1");
        assertThat(updated.getEmotion().getName()).isEqualTo("슬픔1");
        assertThat(visitedList.get(0).getCountry().getCountryCode()).isEqualTo("KR1");
        assertThat(visitedList.get(0).getEmotion().getName()).isEqualTo("슬픔1");
        assertThat(visitedList.get(0).getColor()).isEqualTo("#0000FF");
    }

    @Test
    void testRegisterVisitedCountry_ReturnsSavedEntity() {
        // given
        User user = createTestUser("testKakaoNew");
        Country country = createTestCountry("KRNEW", "대한민국NEW", "🇰🇷");
        Emotion emotion = createTestEmotion("설렘", "#FDD7DE");

        // when
        VisitedCountry result = visitedCountryService.registerVisitedCountry(user.getId(), country.getCountryCode(), emotion.getId());

        // then
        assertThat(result.getVisitedCountryId()).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(user.getId());
        assertThat(result.getCountry().getCountryCode()).isEqualTo("KRNEW");
        assertThat(result.getEmotion().getName()).isEqualTo("설렘");
        assertThat(result.getColor()).isEqualTo("#FDD7DE");
    }

    @Test
    void testAlreadyVisited() {
        // given: 테스트용 데이터 생성
        User user = createTestUser("testKakao2");
        Country country = createTestCountry("KR2", "대한민국2", "🇰🇷");
        Emotion emotion = createTestEmotion("행복2", "#FFD700");
        
        // when & then: 방문 여부 확인
        boolean before = visitedCountryService.alreadyVisited(user.getId(), country.getCountryCode());
        assertThat(before).isFalse();

        visitedCountryService.registerVisitedCountry(user.getId(), country.getCountryCode(), emotion.getId());

        boolean after = visitedCountryService.alreadyVisited(user.getId(), country.getCountryCode());
        assertThat(after).isTrue();
    }

    @Test
    void testGetVisitedCountries() {
        // given: 테스트용 데이터 생성
        User user = createTestUser("testKakao8");
        Country country = createTestCountry("KR8", "대한민국8", "🇰🇷");
        Emotion emotion = createTestEmotion("행복8", "#FFD700");
        
        // 방문 국가 등록
        visitedCountryService.registerVisitedCountry(user.getId(), country.getCountryCode(), emotion.getId());
        
        // when: 방문 국가 목록 조회
        List<VisitedCountry> visitedList = visitedCountryService.getVisitedCountries(user.getId());
        
        // then: 조회된 목록 확인
        assertThat(visitedList).hasSize(1);
        assertThat(visitedList.get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(visitedList.get(0).getCountry().getCountryCode()).isEqualTo(country.getCountryCode());
    }

    // 헬퍼 메서드들
    private User createTestUser(String kakaoId) {
        User user = new User();
        user.setKakaoId(kakaoId);
        user.setFirstName("Hong");
        user.setSurName("Gil");
        user.setStatus(true);
        user.setCreatedAt(LocalDate.now());
        userRepository.save(user);
        return user;
    }

    private Country createTestCountry(String countryCode, String countryName, String emoji) {
        Country country = new Country();
        country.setCountryCode(countryCode);
        country.setCountryName(countryName);
        country.setEmoji(emoji);
        countryRepository.save(country);
        return country;
    }

    private Emotion createTestEmotion(String name, String colorCode) {
        Emotion emotion = new Emotion();
        emotion.setName(name);
        emotion.setColorCode(colorCode);
        emotionRepository.save(emotion);
        return emotion;
    }
} 