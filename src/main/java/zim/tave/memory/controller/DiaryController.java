package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.dto.request.CreateDiaryRequest;
import zim.tave.memory.dto.response.DiaryResponseDto;
import zim.tave.memory.dto.request.UpdateDiaryOptionalFieldsRequest;
import zim.tave.memory.dto.request.UpdateRepresentativeImageRequest;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.DiaryService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "일기 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class DiaryController {

    private final DiaryService diaryService;

	@PostMapping("/users/me/diaries")
    @Operation(summary = "일기 생성", description = "JWT 토큰으로 인증된 사용자의 일기를 생성합니다. 정면/후면 카메라 사진 각 1장씩, 총 2장의 이미지가 필요하며 그 중 1장을 대표 이미지로 설정해야 합니다.")
    @ApiErrorCodeExamples({ErrorCode.IMAGE_COUNT_INVALID, ErrorCode.CAMERA_TYPES_REQUIRED, ErrorCode.REPRESENTATIVE_IMAGE_REQUIRED, ErrorCode.INVALID_COUNTRY_CODE, ErrorCode.VALIDATION_ERROR, ErrorCode.CANNOT_ADD_DIARY_TO_PAST_TRIP, ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.USER_NOT_FOUND, ErrorCode.TRIP_NOT_FOUND, ErrorCode.EMOTION_NOT_FOUND, ErrorCode.WEATHER_NOT_FOUND, ErrorCode.INTERNAL_SERVER_ERROR})
    @ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "일기 생성 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청 데이터입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - IMAGE_COUNT_INVALID: 이미지는 반드시 2장이어야 합니다. (FRONT/BACK 각 1장씩)
                    - CAMERA_TYPES_REQUIRED: FRONT/BACK 카메라 사진이 모두 필요합니다.
                    - REPRESENTATIVE_IMAGE_REQUIRED: 대표 이미지는 정확히 1장이어야 합니다.
                    - INVALID_COUNTRY_CODE: 올바르지 않은 국가 코드입니다.
                    - VALIDATION_ERROR: 요청 값이 올바르지 않습니다. (날짜 형식 오류 포함)
                    - CANNOT_ADD_DIARY_TO_PAST_TRIP: 과거 여행에는 일기를 추가할 수 없습니다.
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
                    - TRIP_NOT_FOUND: 해당 여행을 찾을 수 없습니다.
                    - EMOTION_NOT_FOUND: 감정을 찾을 수 없습니다.
                    - WEATHER_NOT_FOUND: 날씨를 찾을 수 없습니다.
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.",
                    content = @Content)
    })
	public ResponseEntity<ApiResponseDto<DiaryResponseDto>> createDiary(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "일기 생성 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateDiaryRequest.class))
            )
            @RequestBody CreateDiaryRequest request) {
        request.setUserId(userDetails.getUserId());
		DiaryResponseDto dto = diaryService.createDiary(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponseDto.success(ResponseCode.CREATED, dto));
    }

	// 일기 선택 필드 수정 - JWT 인증으로 본인 일기만 수정 가능 (녹음 기능 제외)
	@PatchMapping("/users/me/diaries/{diaryId}")
    @Operation(summary = "일기 선택 필드 수정", description = "JWT 토큰으로 인증된 사용자의 일기 선택적 필드들(감정, 날씨 등)을 수정합니다.")
    @ApiErrorCodeExamples({ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.ACCESS_DENIED, ErrorCode.DIARY_NOT_FOUND, ErrorCode.EMOTION_NOT_FOUND, ErrorCode.WEATHER_NOT_FOUND})
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "일기 수정 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                    - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                    - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.\n- ACCESS_DENIED: 접근 권한이 없습니다.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = """
                    일기를 찾을 수 없습니다. 다음 에러 코드가 발생할 수 있습니다:
                    - DIARY_NOT_FOUND: 일기를 찾을 수 없습니다.
                    - EMOTION_NOT_FOUND: 감정을 찾을 수 없습니다.
                    - WEATHER_NOT_FOUND: 날씨를 찾을 수 없습니다.
                    """,
                    content = @Content)
    })
	public ResponseEntity<ApiResponseDto<Void>> updateDiaryOptionalFields(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "일기 ID", required = true) @PathVariable Long diaryId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "일기 선택 필드 수정 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateDiaryOptionalFieldsRequest.class))
            )
            @RequestBody UpdateDiaryOptionalFieldsRequest request) {
		Long userId = userDetails.getUserId();
		// 소유권 검증은 서비스에서 수행
        diaryService.updateDiaryOptionalFields(userId, diaryId, request);
		return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }

	// 대표 이미지 변경 - JWT 인증으로 본인 일기만 수정 가능
	@PatchMapping("/users/me/diaries/{diaryId}/representative-images")
    @Operation(summary = "대표 이미지 변경", description = "JWT 토큰으로 인증된 사용자의 일기 대표 이미지를 변경합니다.")
    @ApiErrorCodeExamples({ErrorCode.IMAGE_ID_REQUIRED, ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.ACCESS_DENIED, ErrorCode.DIARY_NOT_FOUND, ErrorCode.IMAGE_NOT_FOUND})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대표 이미지 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다. 다음 에러 코드가 발생할 수 있습니다:\n- IMAGE_ID_REQUIRED: 이미지 ID가 필요합니다.",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                    - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                    - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.\n- ACCESS_DENIED: 접근 권한이 없습니다.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = """
                    리소스를 찾을 수 없습니다. 다음 에러 코드가 발생할 수 있습니다:
                    - DIARY_NOT_FOUND: 일기를 찾을 수 없습니다.
                    - IMAGE_NOT_FOUND: 이미지를 찾을 수 없습니다.
                    """,
                    content = @Content)
    })
	public ResponseEntity<ApiResponseDto<Void>> updateRepresentativeImage(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "일기 ID", required = true) @PathVariable Long diaryId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "대표 이미지 변경 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateRepresentativeImageRequest.class))
            )
            @RequestBody UpdateRepresentativeImageRequest request) {
        diaryService.updateRepresentativeImage(diaryId, request.getImageId());
		return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }

    // 사용자별 전체 일기 목록 조회 - JWT 인증으로 개인 데이터 보호
	@GetMapping("/users/me/diaries")
    @Operation(summary = "내 전체 일기 목록 조회", description = "JWT 토큰으로 인증된 사용자의 모든 일기를 조회합니다.")
    @ApiErrorCodeExamples({ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일기 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                    - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                    - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                    """,
                    content = @Content)
    })
	public ResponseEntity<ApiResponseDto<List<DiaryResponseDto>>> getMyAllDiaries(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getUserId();
		List<DiaryResponseDto> diaryDtos = diaryService.findByUserId(userId);
		return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, diaryDtos));
    }

    // 특정 일기 상세 조회 - JWT 인증으로 본인 일기만 조회 가능하도록 보안 강화
	@GetMapping("/users/me/diaries/{diaryId}")
    @Operation(summary = "일기 상세 조회", description = "JWT 토큰으로 인증된 사용자의 특정 일기를 조회합니다.")
    @ApiErrorCodeExamples({ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.ACCESS_DENIED, ErrorCode.DIARY_NOT_FOUND})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일기 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                    - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                    - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.\n- ACCESS_DENIED: 접근 권한이 없습니다.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "일기를 찾을 수 없습니다.\n- DIARY_NOT_FOUND: 일기를 찾을 수 없습니다.",
                    content = @Content)
    })
	public ResponseEntity<ApiResponseDto<DiaryResponseDto>> getDiary(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "일기 ID", required = true) @PathVariable Long diaryId) {
		Long userId = userDetails.getUserId();
		DiaryResponseDto dto = diaryService.findOne(diaryId, userId);
		return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, dto));
    }

    // 내 여행별 일기 목록 조회 - JWT 인증으로 본인 여행의 일기만 조회
	@GetMapping("/users/me/trips/{tripId}/diaries")
    @Operation(summary = "내 여행별 일기 목록 조회", description = "JWT 토큰으로 인증된 사용자의 특정 여행에 속한 모든 일기를 조회합니다.")
    @ApiErrorCodeExamples({ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.ACCESS_DENIED, ErrorCode.TRIP_NOT_FOUND})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "여행 일기 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                    - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                    - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.\n- ACCESS_DENIED: 접근 권한이 없습니다.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없습니다.\n- TRIP_NOT_FOUND: 해당 여행을 찾을 수 없습니다.",
                    content = @Content)
    })
	public ResponseEntity<ApiResponseDto<List<DiaryResponseDto>>> getMyDiariesByTripId(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId) {
        Long userId = userDetails.getUserId();
		List<DiaryResponseDto> diaryDtos = diaryService.findByTripId(tripId, userId);
		return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, diaryDtos));
    }


    // 일기 보관 상태 변경 - JWT 인증으로 본인 일기만 보관 가능
	@PatchMapping("/users/me/diaries/{diaryId}/store")
    @Operation(summary = "일기 보관 상태 변경", description = "JWT 토큰으로 인증된 사용자의 특정 일기를 보관하거나 보관 해제합니다.")
    @ApiErrorCodeExamples({ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.ACCESS_DENIED, ErrorCode.DIARY_NOT_FOUND})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보관 상태 변경 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                    - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                    - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.\n- ACCESS_DENIED: 접근 권한이 없습니다.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "일기를 찾을 수 없습니다.\n- DIARY_NOT_FOUND: 일기를 찾을 수 없습니다.",
                    content = @Content)
    })
	public ResponseEntity<ApiResponseDto<Void>> storeDiary(
            @Parameter(description = "일기 ID", required = true) @PathVariable Long diaryId,
            @RequestParam boolean isStored,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId) {
        diaryService.storeDiary(diaryId, userId, isStored);
		return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }

    // 일기 삭제 - JWT 인증으로 본인 일기만 삭제 가능
	@DeleteMapping("/users/me/diaries/{diaryId}")
    @Operation(summary = "일기 삭제", description = "JWT 토큰으로 인증된 사용자의 특정 일기를 삭제합니다.")
    @ApiErrorCodeExamples({ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.ACCESS_DENIED, ErrorCode.DIARY_NOT_FOUND})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일기 삭제 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                    - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                    - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.\n- ACCESS_DENIED: 접근 권한이 없습니다.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "일기를 찾을 수 없습니다.\n- DIARY_NOT_FOUND: 일기를 찾을 수 없습니다.",
                    content = @Content)
    })
	public ResponseEntity<ApiResponseDto<Void>> deleteDiary(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "일기 ID", required = true) @PathVariable Long diaryId) {
        Long userId = userDetails.getUserId();
        diaryService.deleteDiary(diaryId, userId);
		return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }
}
