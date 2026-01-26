package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "kakaoLogin-controller", description = "카카오 로그인")
public class LoginController {
    /*
    토큰 발급 URL
    https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=8faa2ba724cbfef5c2abf54ebf9bed65&redirect_uri=http://localhost:8080/callback
    */
    private final LoginService loginService;
    private final KakaoOAuthService kakaoOAuthService;
    private final KakaoApiClient kakaoApiClient;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

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
            @RequestParam String code) {

        // 1. 인가 코드로 카카오 액세스 토큰 획득
        String kakaoAccessToken = kakaoOAuthService.getAccessToken(code);

        // 2. 카카오 액세스 토큰으로 로그인 처리
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setAccessToken(kakaoAccessToken);

        LoginResponseDto response = loginService.login(loginRequest);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.LOGIN_SUCCESS, response));
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
            @RequestHeader("Authorization") String refreshTokenHeader) {

        if (refreshTokenHeader == null || !refreshTokenHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String refreshToken = refreshTokenHeader.substring(7); // "Bearer " 제거

        // Refresh Token 검증
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 새 Access Token 발급
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getKakaoId());

        TokenRefreshResponse response = TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .build();

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.TOKEN_REFRESH_SUCCESS, response));
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
    public ResponseEntity<ApiResponseDto<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        loginService.logout(userId);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.LOGOUT_SUCCESS, null));
    }
}
