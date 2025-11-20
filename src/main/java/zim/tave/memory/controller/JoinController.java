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
import zim.tave.memory.dto.request.JoinRequestDto;
import zim.tave.memory.dto.response.UserResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.service.JoinService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "join-controller", description = "회원가입(카카오 로그인 후 사용자 정보 입력)")
@SecurityRequirements
public class JoinController {
    private final JoinService joinService;

    @Operation(summary = "회원가입용 정보 입력",
            description = "카카오 로그인 후 사용자 정보 입력하여 계정 생성"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.KAKAO_LOGIN_REQUIRED,
            ErrorCode.ALREADY_JOINED
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - KAKAO_LOGIN_REQUIRED: 카카오 로그인이 필요합니다.
                    """),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED
                    - INVALID_TOKEN
                    - UNAUTHORIZED_USER
                    """),
            @ApiResponse(responseCode = "409", description = """
                    중복 가입 오류입니다:
                    - ALREADY_JOINED: 이미 가입된 사용자입니다.
                    """)
    })
    @PostMapping("/join")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> join(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody JoinRequestDto requestDto) {

        User user = joinService.join(userId, requestDto);

        UserResponseDto response = UserResponseDto.from(user);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.JOIN_SUCCESS, response));
    }
}
