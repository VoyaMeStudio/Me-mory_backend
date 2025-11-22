package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.dto.request.UpdateUserRequestDto;
import zim.tave.memory.dto.response.UserResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.SettingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Setting-controller", description = "설정 조회(로그아웃, 회원 탈퇴)")
@SecurityRequirement(name = "bearerAuth")
public class SettingController {

    private final SettingService settingService;

    @Operation(summary = "회원탈퇴", description = "사용자 계정을 삭제")
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED
                - INVALID_TOKEN
                - UNAUTHORIZED_USER
                """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
                리소스를 찾을 수 없습니다. 다음 에러 코드가 발생할 수 있습니다:
                - USER_NOT_FOUND: 사용자를 찾을 수 없습니다.
                """, content = @Content),
            @ApiResponse(responseCode = "500", description = """
                서버 내부 오류입니다:
                - INTERNAL_SERVER_ERROR
                """, content = @Content)
    })
    @DeleteMapping("/users/me")
    public ResponseEntity<ApiResponseDto<Void>> deleteAccount(
            @AuthenticationPrincipal(expression = "userId") Long userId) {

        settingService.deleteAccount(userId);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.USER_DELETE_SUCCESS, null));
    }

    @Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 이름, 생년월일, 국적 정보를 수정합니다.")
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.VALIDATION_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = """
                잘못된 요청입니다. 다음 오류 코드가 발생할 수 있습니다:
                - VALIDATION_ERROR: 수정할 필드가 존재하지 않거나 형식이 올바르지 않습니다.
                """, content = @Content),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 오류가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED
                - INVALID_TOKEN
                - UNAUTHORIZED_USER
                """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
                리소스를 찾을 수 없습니다:
                - USER_NOT_FOUND: 사용자를 찾을 수 없습니다.
                """, content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류로 인해 회원 정보 수정 실패")
    })
    @PutMapping("/users/me")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUserInfo(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody UpdateUserRequestDto requestDto) {

        UserResponseDto updatedUser = settingService.updateUserInfo(userId, requestDto);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.USER_UPDATE_SUCCESS, updatedUser));
    }

    @Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 이름, 생년월일, 국적 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "수정할 필드가 존재하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류로 인해 회원 정보 수정 실패")
    })
    @PutMapping("/users/me")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateUserRequestDto requestDto) {

        Long userId = userDetails.getUserId();
        UserResponseDto updatedUser = settingService.updateUserInfo(userId, requestDto);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.USER_UPDATE_SUCCESS, updatedUser));
    }
}
