package zim.tave.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.JoinRequestDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JoinServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JoinService joinService;

    private User user;
    private JoinRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setKakaoId("testKakaoId000");
        user.setProfileImageUrl("https://test.com/image.jpg");
        user.setRegistered(false);

        requestDto = new JoinRequestDto(
                "sur",
                "first",
                "최형원",
                LocalDate.of(2222, 2, 2),
                "REPUBLIC OF KOREA",
                true
        );
    }

    @Test
    void 회원가입_성공_테스트() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User savedUser = joinService.join(1L, requestDto);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getKakaoId()).isEqualTo("testKakaoId000");
        assertThat(savedUser.getProfileImageUrl()).isEqualTo("https://test.com/image.jpg");
        assertThat(savedUser.getSurName()).isEqualTo("sur");
        assertThat(savedUser.getFirstName()).isEqualTo("first");
        assertThat(savedUser.getKoreanName()).isEqualTo("최형원");
        assertThat(savedUser.getBirth()).isEqualTo(LocalDate.of(2222, 2, 2));
        assertThat(savedUser.getNationality()).isEqualTo("REPUBLIC OF KOREA");
        assertThat(savedUser.getDiaryCount()).isEqualTo(0L);
        assertThat(savedUser.getVisitedCountryCount()).isEqualTo(0L);
        assertThat(savedUser.getFlags()).isEqualTo("");
        assertThat(savedUser.isRegistered()).isTrue();
        assertThat(savedUser.isStatus()).isTrue();

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void 사용자_없음_예외_테스트() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> joinService.join(1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void 카카오_로그인_필수_예외_테스트() {
        // given
        user.setKakaoId(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> joinService.join(1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.KAKAO_LOGIN_REQUIRED);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void 이미_가입된_회원_예외_테스트() {
        // given
        user.setRegistered(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> joinService.join(1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_JOINED);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
}
