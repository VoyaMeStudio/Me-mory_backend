package zim.tave.memory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.LoginRequestDto;
import zim.tave.memory.dto.response.LoginResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.kakao.KakaoApiClient;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.security.JwtAuthenticationFilter;
import zim.tave.memory.service.KakaoOAuthService;
import zim.tave.memory.service.LoginService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LoginService loginService;

    @MockBean
    KakaoOAuthService kakaoOAuthService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    UserRepository userRepository;

    @MockBean
    private KakaoApiClient kakaoApiClient;

    @Test
    @DisplayName("카카오 로그인 (인가 코드 방식) - 성공")
    void 카카오_로그인_인가코드_성공() throws Exception {
        // given
        String authCode = "test_auth_code";
        String kakaoAccessToken = "kakao_access_token";

        LoginResponseDto response = new LoginResponseDto(
                1L,
                true,
                "4317757086",
                "http://img1.kakaocdn.net/profile.jpeg",
                "jwt_access_token",
                "jwt_refresh_token"
        );

        when(kakaoOAuthService.getAccessToken(authCode)).thenReturn(kakaoAccessToken);
        when(loginService.login(any(LoginRequestDto.class))).thenReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/auth/login/kakao")
                                .param("code", "valid_auth_code")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code")
                        .value(ResponseCode.LOGIN_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.accessToken")
                        .value("jwt_access_token"))
                .andExpect(jsonPath("$.data.refreshToken")
                        .value("jwt_refresh_token"));

    }


    @Test
    @DisplayName("카카오 로그인 (액세스 토큰 직접 전달) - 성공")
    void 카카오_로그인_토큰_성공() throws Exception {
        // given
        String kakaoAccessToken = "valid_kakao_token";

        LoginResponseDto response = new LoginResponseDto(
                1L,
                true,
                "4317757086",
                "http://img1.kakaocdn.net/profile.jpeg",
                "jwt_access_token",
                "jwt_refresh_token"
        );

        when(kakaoOAuthService.getAccessToken(any())).thenReturn(kakaoAccessToken);
        when(loginService.login(any(LoginRequestDto.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/auth/login/kakao")
                        .param("code", "valid_code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.LOGIN_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.accessToken").value("jwt_access_token"))
                .andExpect(jsonPath("$.data.refreshToken").value("jwt_refresh_token"));
    }

    @Test
    @DisplayName("신규 회원 로그인 - registered = false")
    void 신규회원_로그인() throws Exception {
        // given
        String authCode = "new_user_code";
        String kakaoAccessToken = "new_user_kakao_token";

        LoginResponseDto response = new LoginResponseDto(
                10L,
                false, // 앱 가입 미완료
                "9999999999",
                "http://new-profile.jpg",
                "new_access_token",
                "new_refresh_token"
        );

        when(kakaoOAuthService.getAccessToken(authCode)).thenReturn(kakaoAccessToken);
        when(loginService.login(any(LoginRequestDto.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/auth/login/kakao")
                        .param("code", authCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.registered").value(false))
                .andExpect(jsonPath("$.data.userId").value(10L));
    }

    @Test
    @DisplayName("로그인 실패 - 토큰 누락")
    void 로그인_토큰누락_400() throws Exception {
        // given - 서비스 레벨에서 검증되므로 인가코드 방식으로 테스트
        String authCode = "test_code";

        when(kakaoOAuthService.getAccessToken(authCode)).thenReturn("");
        when(loginService.login(any()))
                .thenThrow(new CustomException(ErrorCode.KAKAO_TOKEN_MISSING));

        // when & then
        mockMvc.perform(get("/api/auth/login/kakao")
                        .param("code", authCode))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));  // GlobalExceptionHandler가 HTTP 상태코드를 반환
    }

    @Test
    @DisplayName("로그인 실패 - 카카오 토큰 요청 실패")
    void 카카오_토큰요청_실패() throws Exception {
        // given
        String invalidCode = "invalid_code";

        when(kakaoOAuthService.getAccessToken(invalidCode))
                .thenThrow(new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED));

        // when & then
        mockMvc.perform(get("/api/auth/login/kakao")
                        .param("code", invalidCode))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(500));  // GlobalExceptionHandler가 500을 반환
    }

    @Test
    @DisplayName("토큰 갱신 - 성공")
    void 토큰_갱신_성공() throws Exception {
        // given
        String refreshToken = "valid_refresh_token";
        User user = new User();
        user.setId(1L);
        user.setKakaoId("kakao123");

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(refreshToken)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(1L, "kakao123")).thenReturn("new_access_token");

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.TOKEN_REFRESH_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.accessToken").value("new_access_token"));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 Refresh Token")
    void 토큰_갱신_실패_유효하지않음() throws Exception {
        // given
        String invalidRefreshToken = "invalid_refresh_token";

        when(jwtUtil.validateToken(invalidRefreshToken)).thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + invalidRefreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(500));  // GlobalExceptionHandler가 500을 반환
    }


    @Test
    @DisplayName("토큰 갱신 실패 - Access Token 사용")
    void 토큰_갱신_실패_액세스토큰() throws Exception {
        // given
        String accessToken = "access_token_not_refresh";

        when(jwtUtil.validateToken(accessToken)).thenReturn(true);
        when(jwtUtil.isRefreshToken(accessToken)).thenReturn(false); // Access Token

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(500));  // GlobalExceptionHandler가 500을 반환
    }

    @Test
    @DisplayName("토큰 갱신 실패 - Authorization 헤더 누락")
    void 토큰_갱신_실패_헤더누락() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void 로그아웃_성공() throws Exception {
        // given
        CustomUserDetails principal = new CustomUserDetails(1L, "kakao_123");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        mockMvc.perform(patch("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.LOGOUT_SUCCESS.getCode()));

        Mockito.verify(loginService).logout(1L);
    }
}
