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
import zim.tave.memory.dto.UpdateUserRequestDto;
import zim.tave.memory.dto.UserResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원탈퇴 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류, 존재하지 않는 사용자 등", content = @Content())
    })
    @DeleteMapping("/users/me")
    public ResponseEntity<ApiResponseDto<Void>> deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        settingService.deleteAccount(userId);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.USER_DELETE_SUCCESS, null));
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
