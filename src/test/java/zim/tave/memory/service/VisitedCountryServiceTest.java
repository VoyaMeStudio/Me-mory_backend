package zim.tave.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.Emotion;
import zim.tave.memory.domain.User;
import zim.tave.memory.domain.VisitedCountry;
import zim.tave.memory.dto.response.VisitedCountryResponseDto;
import zim.tave.memory.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VisitedCountryServiceTest {

    @Mock
    private VisitedCountryRepository visitedCountryRepository;
    @InjectMocks
    private VisitedCountryService visitedCountryService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CountryService countryService;
    @Mock
    private EmotionRepository emotionRepository;

    private User user;
    private Country country;
    private Emotion emotion1;
    private Emotion emotion2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setKakaoId("testKakao1");
        user.setStatus(true);
        user.setCreatedAt(LocalDate.now());

        country = new Country("KR", "대한민국", "🇰🇷");

        emotion1 = new Emotion("행복1", "#FFD700");
        emotion1.setId(1L);

        emotion2 = new Emotion("슬픔1", "#0000FF");
        emotion2.setId(2L);
    }

    @Test
    void testRegisterVisitedCountry_SuccessAndUpdate() {
        // given: 테스트용 데이터 생성
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(countryService.findByCode("KR")).thenReturn(country);
        when(emotionRepository.findById(1L)).thenReturn(Optional.of(emotion1));
        when(emotionRepository.findById(2L)).thenReturn(Optional.of(emotion2));

        VisitedCountry existingVisited = new VisitedCountry();
        existingVisited.setVisitedCountryId(1L);
        existingVisited.setUser(user);
        existingVisited.setCountry(country);
        existingVisited.setEmotion(emotion1);
        existingVisited.setColor(emotion1.getColorCode());

        // 첫 번째 등록 시 존재하지 않음, 두 번째 등록 시 기존 레코드 반환
        when(visitedCountryRepository.findByUserIdAndCountryCodeWithDetails(1L, "KR"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingVisited));
        when(visitedCountryRepository.save(any(VisitedCountry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when - 첫 번째 등록
        VisitedCountryResponseDto first = visitedCountryService.registerVisitedCountry(1L, "KR", 1L);

        // when - 두 번째 등록 (감정 변경)
        VisitedCountryResponseDto updated = visitedCountryService.registerVisitedCountry(1L, "KR", 2L);

        // then
        assertThat(first.getEmotionName()).isEqualTo("행복1");
        assertThat(updated.getEmotionName()).isEqualTo("슬픔1");
    }

    @Test
    void testRegisterVisitedCountry_ReturnsSavedEntity() {
        // given
        Country usCountry = new Country("US", "미국", "🇺🇸");
        Emotion emotion = new Emotion("설렘", "#FDD7DE");
        emotion.setId(3L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(countryService.findByCode("US")).thenReturn(usCountry);
        when(emotionRepository.findById(3L)).thenReturn(Optional.of(emotion));
        when(visitedCountryRepository.findByUserIdAndCountryCodeWithDetails(1L, "US"))
                .thenReturn(Optional.empty());
        when(visitedCountryRepository.save(any(VisitedCountry.class))).thenAnswer(invocation -> {
            VisitedCountry vc = invocation.getArgument(0);
            vc.setVisitedCountryId(10L);
            return vc;
        });

        // when
        VisitedCountryResponseDto result =
                visitedCountryService.registerVisitedCountry(1L, "US", 3L);

        // then
        assertThat(result.getVisitedCountryId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getCountryCode()).isEqualTo("US");
        assertThat(result.getEmotionName()).isEqualTo("설렘");
        assertThat(result.getColor()).isEqualTo("#FDD7DE");
    }

    @Test
    void testAlreadyVisited() {
        // given
        when(visitedCountryRepository.existsByUserIdAndCountryCode(1L, "JP"))
                .thenReturn(false)
                .thenReturn(true);

        // when & then: 방문 여부 화인
        boolean before = visitedCountryService.alreadyVisited(1L, "JP");
        assertThat(before).isFalse();

        boolean after = visitedCountryService.alreadyVisited(1L, "JP");
        assertThat(after).isTrue();
    }

    @Test
    void testGetVisitedCountries() {
        // given: 테스트용 데이터 생성
        Country cn = new Country("CN", "중국", "🇨🇳");
        Emotion emo = new Emotion("행복8", "#FFD700");
        emo.setId(8L);

        VisitedCountry visited = new VisitedCountry();
        visited.setVisitedCountryId(100L);
        visited.setUser(user);
        visited.setCountry(cn);
        visited.setEmotion(emo);
        visited.setColor(emo.getColorCode());

        when(visitedCountryRepository.findByUserIdWithDetails(1L))
                .thenReturn(List.of(visited));

        // when
        List<VisitedCountryResponseDto> visitedList = visitedCountryService.getVisitedCountries(1L);

        // then
        assertThat(visitedList).hasSize(1);
        assertThat(visitedList.get(0).getUserId()).isEqualTo(1L);
        assertThat(visitedList.get(0).getCountryCode()).isEqualTo("CN");
    }

}
