package zim.tave.memory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.User;
import zim.tave.memory.domain.VisitedCountry;
import zim.tave.memory.dto.response.MyPageResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.DiaryRepository;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.repository.VisitedCountryRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MyPageServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private VisitedCountryRepository visitedCountryRepository;

    @InjectMocks
    private MyPageService myPageService;

    @Test
    void 마이페이지조회_성공() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setSurName("KANG");
        user.setFirstName("JIHYE");
        user.setKoreanName("강지혜");
        user.setBirth(LocalDate.of(2000, 2, 22));
        user.setNationality("REPUBLIC OF KOREA");
        user.setProfileImageUrl("http://img.test/profile.jpg");

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(diaryRepository.countByUser_Id(anyLong()))
                .thenReturn(5L);

        when(visitedCountryRepository.countByUserId(anyLong()))
                .thenReturn(2L);

        Country korea = new Country();
        korea.setEmoji("🇰🇷");

        Country japan = new Country();
        japan.setEmoji("🇯🇵");

        VisitedCountry v1 = new VisitedCountry();
        v1.setCountry(korea);

        VisitedCountry v2 = new VisitedCountry();
        v2.setCountry(japan);

        when(visitedCountryRepository.findByUserIdWithDetails(userId))
                .thenReturn(List.of(v1, v2));

        MyPageResponseDto result = myPageService.getMyPage(userId);

        assertThat(result.getUser().getUserId()).isEqualTo(userId);
        assertThat(result.getUser().getSurName()).isEqualTo("KANG");
        assertThat(result.getStatistics().getDiaryCount()).isEqualTo(5L);
        assertThat(result.getStatistics().getCountryCount()).isEqualTo(2L);
        assertThat(result.getFlags()).isEqualTo("🇰🇷🇯🇵");

    }

    @Test
    void 유저없음_USER_NOT_FOUND() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> myPageService.getMyPage(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 필수정보누락_INCOMPLETE_USER_INFO() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setSurName(null);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> myPageService.getMyPage(userId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INCOMPLETE_USER_INFO.getMessage());
    }
}
