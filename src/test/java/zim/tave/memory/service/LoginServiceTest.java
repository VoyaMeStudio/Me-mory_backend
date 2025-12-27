package zim.tave.memory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.LoginRequestDto;
import zim.tave.memory.dto.response.LoginResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.kakao.KakaoApiClient;
import zim.tave.memory.kakao.KakaoUserInfo;
import zim.tave.memory.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;  // 추가

    @InjectMocks
    private LoginService loginService;

    @Test
    void 토큰_누락시_예외() {
        LoginRequestDto request = new LoginRequestDto("");

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.KAKAO_TOKEN_MISSING.getMessage());
    }

    @Test
    void 기존회원_로그인() {
        LoginRequestDto request = new LoginRequestDto("valid_token");

        KakaoUserInfo kakaoInfo = new KakaoUserInfo("kakao123", "http://img.jpg");
        User user = new User();
        user.setId(1L);
        user.setKakaoId("kakao123");

        when(kakaoApiClient.getKakaoUserInfo("valid_token")).thenReturn(kakaoInfo);
        when(userRepository.findByKakaoId("kakao123")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(), any())).thenReturn("jwt_token");

        LoginResponseDto response = loginService.login(request);

        assertThat(response.isRegistered()).isTrue();
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void 신규회원_로그인() {
        LoginRequestDto request = new LoginRequestDto("valid_token");

        KakaoUserInfo kakaoInfo = new KakaoUserInfo("kakao123", "http://img.jpg");

        when(kakaoApiClient.getKakaoUserInfo("valid_token")).thenReturn(kakaoInfo);
        when(userRepository.findByKakaoId("kakao123")).thenReturn(Optional.empty());
        when(jwtUtil.generateToken(any(), any())).thenReturn("jwt_token");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        LoginResponseDto response = loginService.login(request);

        assertThat(response.isRegistered()).isFalse();
        assertThat(response.getUserId()).isEqualTo(10L);
    }

    @Test
    void 로그아웃_성공() {
        User user = new User();
        user.setId(1L);
        user.setStatus(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        loginService.logout(1L);

        assertThat(user.isStatus()).isFalse();
    }

    @Test
    void 이미_로그아웃된_사용자() {
        User user = new User();
        user.setId(1L);
        user.setStatus(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> loginService.logout(1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_LOGGED_OUT.getMessage());
    }
}
