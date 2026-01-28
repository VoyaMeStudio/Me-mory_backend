package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.LoginRequestDto;
import zim.tave.memory.dto.response.LoginResponseDto;
import zim.tave.memory.dto.response.TokenRefreshResponse;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.kakao.KakaoApiClient;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.KakaoOAuthService;
import zim.tave.memory.service.LoginService;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "kakaoLogin-controller", description = "카카오 로그인")
public class LoginController {

    private final LoginService loginService;
    private final KakaoOAuthService kakaoOAuthService;
    private final KakaoApiClient kakaoApiClient;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 14 * 24 * 60 * 60; // 14일 (초 단위)


    @Operation(summary = "카카오 로그인 (인가 코드 방식)",
            description = """
                카카오 OAuth 인가 코드를 받아 로그인 처리 및 JWT 토큰 발급

                프론트엔드는 먼저 카카오 로그인 URL로 리다이렉트:
                https://kauth.kakao.com/oauth/authorize?client_id={CLIENT_ID}&redirect_uri={REDIRECT_URI}&response_type=code

                카카오에서 받은 code를 이 엔드포인트로 전달하면 자체 JWT(Access Token + Refresh Token) 발급
                """)
    @SecurityRequirements
    @ApiErrorCodeExamples({
            ErrorCode.KAKAO_TOKEN_REQUEST_FAILED,
            ErrorCode.KAKAO_INVALID_TOKEN,
            ErrorCode.KAKAO_UNAUTHORIZED,
            ErrorCode.KAKAO_SERVER_ERROR,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 - JWT 토큰 발급 완료"),
            @ApiResponse(responseCode = "400", description = """
                잘못된 요청입니다. 다음 오류가 발생할 수 있습니다:
                - KAKAO_TOKEN_REQUEST_FAILED: 카카오 토큰 요청에 실패했습니다.
                """, content = @Content),
            @ApiResponse(responseCode = "401", description = """
                카카오 인증 실패입니다. 다음 오류가 발생할 수 있습니다:
                - KAKAO_INVALID_TOKEN: 유효하지 않거나 만료된 카카오 Access Token입니다.
                - KAKAO_UNAUTHORIZED: 카카오 API 접근 권한이 없습니다.
                """, content = @Content),
            @ApiResponse(responseCode = "500", description = """
                서버 오류입니다. 다음 오류가 발생할 수 있습니다:
                - KAKAO_SERVER_ERROR: 카카오 서버 문제로 사용자 정보를 가져올 수 없습니다.
                - INTERNAL_SERVER_ERROR: 로그인 처리 중 알 수 없는 오류가 발생했습니다.
                """, content = @Content)
    })
    @GetMapping("/login/kakao")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> kakaoLoginWithCode(
            @RequestParam String code,
            HttpServletResponse response) {

        // 1. 인가 코드로 카카오 액세스 토큰 획득
        String kakaoAccessToken = kakaoOAuthService.getAccessToken(code);

        // 2. 카카오 액세스 토큰으로 로그인 처리
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setAccessToken(kakaoAccessToken);

        // 3. 로그인 처리 및 토큰 발급
        String[] tokens = loginService.loginAndGenerateTokens(loginRequest);
        String accessToken = tokens[0];
        String refreshToken = tokens[1];
        User user = loginService.getUserByKakaoAccessToken(kakaoAccessToken);

        // 4. Refresh Token을 HttpOnly 쿠키로 설정
        setRefreshTokenCookie(response, refreshToken);

        // 5. Access Token만 응답 바디로 반환
        LoginResponseDto loginResponse = LoginResponseDto.from(user, accessToken, user.isRegistered());
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.LOGIN_SUCCESS, loginResponse));
    }

    @Operation(summary = "토큰 갱신",
            description = """
                Refresh Token으로 새로운 Access Token 발급

                Access Token이 만료되었을 때 사용
                Authorization Header에 'Bearer {refreshToken}' 형식으로 전달
                """)
    @SecurityRequirements
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_REFRESH_TOKEN,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.AUTHENTICATION_FAILED
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 오류가 발생할 수 있습니다:
                - INVALID_REFRESH_TOKEN: 유효하지 않은 Refresh Token입니다.
                - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
                리소스를 찾을 수 없습니다. 다음 오류가 발생할 수 있습니다:
                - USER_NOT_FOUND: 사용자를 찾을 수 없습니다.
                """, content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<TokenRefreshResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        // 1. HttpOnly 쿠키에서 Refresh Token 추출
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. Refresh Token 검증
        jwtUtil.validateTokenWithException(refreshToken);

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 3. 새 Access Token 발급
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getKakaoId());

        // 4. 새 Refresh Token도 발급하여 쿠키 갱신 (Refresh Token Rotation)
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
        setRefreshTokenCookie(response, newRefreshToken);

        TokenRefreshResponse tokenResponse = TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .build();

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.TOKEN_REFRESH_SUCCESS, tokenResponse));
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃 처리")
    @SecurityRequirement(name = "bearerAuth")
    @ApiErrorCodeExamples({
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.ALREADY_LOGGED_OUT,
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = """
                잘못된 요청입니다. 다음 에러 코드가 발생할 수 있습니다:
                - ALREADY_LOGGED_OUT: 이미 로그아웃된 사용자입니다.
                """,
                    content = @Content),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                """,
                    content = @Content),
            @ApiResponse(responseCode = "404", description = """
                리소스를 찾을 수 없습니다. 다음 에러 코드가 발생할 수 있습니다:
                - USER_NOT_FOUND: 사용자를 찾을 수 없습니다.
                """,
                    content = @Content)
    })
    @PatchMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response) {

        Long userId = userDetails.getUserId();
        loginService.logout(userId);

        // Refresh Token 쿠키 삭제
        clearRefreshTokenCookie(response);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.LOGOUT_SUCCESS, null));
    }

    /**
     * HttpOnly 쿠키에 Refresh Token 설정
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);  // XSS 공격 방지
        cookie.setSecure(true);    // HTTPS에서만 전송 (프로덕션 환경)
        cookie.setPath("/");       // 모든 경로에서 쿠키 전송
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_MAX_AGE);  // 14일
        response.addCookie(cookie);
    }

    /**
     * HttpOnly 쿠키에서 Refresh Token 추출
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Refresh Token 쿠키 삭제 (로그아웃 시)
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 즉시 만료
        response.addCookie(cookie);
    }
}
