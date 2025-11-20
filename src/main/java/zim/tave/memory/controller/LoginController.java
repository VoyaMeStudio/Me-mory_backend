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
import zim.tave.memory.dto.LoginRequestDto;
import zim.tave.memory.dto.LoginResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.security.CustomUserDetails;
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

    @Operation(summary = "카카오 로그인 요청",
            description = "카카오 사용자 인증 후 토큰 발급, 토큰 이용하여 백엔드 서버가 로그인 처리")
    @SecurityRequirements
    @ApiErrorCodeExamples({ErrorCode.INVALID_TOKEN, ErrorCode.INTERNAL_SERVER_ERROR})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (client id 오류, 만료된 authorization code 등)", content = @Content()),
            @ApiResponse(responseCode = "500", description = "서버 오류, 유효하지 않은 토큰", content = @Content())
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> kakaoLogin(
            @RequestBody LoginRequestDto request) {
        LoginResponseDto response = loginService.login(request);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.LOGIN_SUCCESS, response));
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
