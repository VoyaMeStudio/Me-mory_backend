package zim.tave.memory.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.dto.response.StoredTripListResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.StorageService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage")
@Tag(name = "Storage", description = "보관된 여행/일기 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class StorageController {

    private final StorageService storageService;

    @Operation(summary = "보관된 여행 목록 조회", description = "로그인한 사용자의 보관된(숨긴) 여행 목록을 조회합니다.")
    @ApiErrorCodeExamples({ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.INTERNAL_SERVER_ERROR})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보관된 여행 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                    - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                    - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "보관된 여행 정보를 찾을 수 없습니다.\n- STORED_TRIP_NOT_FOUND: 보관된 여행 정보를 찾을 수 없습니다.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.",
                    content = @Content)
    })
    @GetMapping("/trips")
    public ResponseEntity<ApiResponseDto<StoredTripListResponseDto>> getStoredTrips(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        StoredTripListResponseDto storedTrips = storageService.getStoredTrips(userId);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.STORED_TRIP_FOUND_SUCCESS, storedTrips));
    }
}
