package zim.tave.memory.service;

import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private LoginService loginService;

    @Test
    @DisplayName("토큰이 null인 경우 예외 발생")
    void 토큰_null시_예외() {
        // given
        LoginRequestDto request = new LoginRequestDto();
        request.setAccessToken(null);

        // when & then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.KAKAO_TOKEN_MISSING.getMessage());
    }

    @Test
    void 토큰_누락시_예외() {
        LoginRequestDto request = new LoginRequestDto("");

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.KAKAO_TOKEN_MISSING.getMessage());
    }

    @Test
    @DisplayName("기존 회원 로그인 - Access Token과 Refresh Token 발급")
    void 기존회원_로그인() {
        // given
        LoginRequestDto request = new LoginRequestDto("valid_kakao_token");

        KakaoUserInfo kakaoInfo = new KakaoUserInfo("kakao123", "http://img.jpg");
        User user = new User();
        user.setId(1L);
        user.setKakaoId("kakao123");
        user.setRegistered(true); // 앱 가입 완료 상태

        when(kakaoApiClient.getKakaoUserInfo("valid_kakao_token")).thenReturn(kakaoInfo);
        when(userRepository.findByKakaoId("kakao123")).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(anyLong(), anyString())).thenReturn("access_token_123");
        when(jwtUtil.generateRefreshToken(anyLong())).thenReturn("refresh_token_456");

        // when
        LoginResponseDto response = loginService.login(request);

        // then
        assertThat(response.isRegistered()).isTrue();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getKakaoId()).isEqualTo("kakao123");
        assertThat(response.getAccessToken()).isEqualTo("access_token_123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token_456");

        verify(jwtUtil, times(1)).generateAccessToken(1L, "kakao123");
        verify(jwtUtil, times(1)).generateRefreshToken(1L);
    }

    @Test
    @DisplayName("신규 회원 로그인 - 카카오 로그인만 완료, 앱 가입 미완료")
    void 신규회원_로그인() {
        // given
        LoginRequestDto request = new LoginRequestDto("valid_kakao_token");

        KakaoUserInfo kakaoInfo = new KakaoUserInfo("kakao999", "http://profile.jpg");

        when(kakaoApiClient.getKakaoUserInfo("valid_kakao_token")).thenReturn(kakaoInfo);
        when(userRepository.findByKakaoId("kakao999")).thenReturn(Optional.empty());
        when(jwtUtil.generateAccessToken(anyLong(), anyString())).thenReturn("new_access_token");
        when(jwtUtil.generateRefreshToken(anyLong())).thenReturn("new_refresh_token");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        // when
        LoginResponseDto response = loginService.login(request);

        // then
        assertThat(response.isRegistered()).isFalse(); // 앱 가입 미완료
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getKakaoId()).isEqualTo("kakao999");
        assertThat(response.getAccessToken()).isEqualTo("new_access_token");
        assertThat(response.getRefreshToken()).isEqualTo("new_refresh_token");

        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateAccessToken(10L, "kakao999");
        verify(jwtUtil, times(1)).generateRefreshToken(10L);
    }

    @Test
    void 로그아웃_성공() {
        // given
        User user = new User();
        user.setId(1L);
        user.setStatus(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        loginService.logout(1L);

        // then
        assertThat(user.isStatus()).isFalse();
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void 이미_로그아웃된_사용자() {
        // given
        User user = new User();
        user.setId(1L);
        user.setStatus(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> loginService.logout(1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_LOGGED_OUT.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 로그아웃 - 예외 발생")
    void 존재하지않는_사용자_로그아웃() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> loginService.logout(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("카카오 API 호출 실패 시 예외 전파")
    void 카카오_API_실패() {
        // given
        LoginRequestDto request = new LoginRequestDto("invalid_kakao_token");

        when(kakaoApiClient.getKakaoUserInfo("invalid_kakao_token"))
                .thenThrow(new CustomException(ErrorCode.KAKAO_INVALID_TOKEN));

        // when & then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.KAKAO_INVALID_TOKEN.getMessage());
    }

}
