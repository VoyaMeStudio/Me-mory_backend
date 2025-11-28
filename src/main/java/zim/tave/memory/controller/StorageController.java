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
import org.springframework.web.bind.annotation.*;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.dto.response.StoredDiaryListResponseDto;
import zim.tave.memory.dto.response.StoredTripListResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.StorageService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage")
@Tag(name = "Storage", description = "보관된 여행/일기 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class StorageController {

    private final StorageService storageService;

    @Operation(summary = "보관된 여행 목록 조회",
            description = "로그인한 사용자의 보관된(숨긴) 여행 목록을 조회합니다. 여행 보관시 해당 여행의 일기도 모두 보관됨")
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보관된 여행 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED
                - INVALID_TOKEN
                - UNAUTHORIZED_USER
                """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
                사용자 정보를 찾을 수 없습니다:
                - USER_NOT_FOUND
                """, content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.", content = @Content)
    })
    @GetMapping("/trips")
    public ResponseEntity<ApiResponseDto<StoredTripListResponseDto>> getStoredTrips(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId) {

        StoredTripListResponseDto storedTrips = storageService.getStoredTrips(userId);

        return ResponseEntity.ok(
                ApiResponseDto.success(ResponseCode.STORED_TRIP_FOUND_SUCCESS, storedTrips)
        );
    }

    @Operation(summary = "보관된 여행 게시",
            description = "보관된(숨긴) 여행을 다시 게시하여 공개 상태로 되돌립니다. 해당 여행에 포함된 일기도 다시 게시")
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.TRIP_NOT_FOUND,
            ErrorCode.TRIP_UPDATE_FORBIDDEN,
            ErrorCode.TRIP_NOT_STORED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보관 해제(게시) 성공"),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                """,
                    content = @Content),
            @ApiResponse(responseCode = "403", description = """
                권한이 없습니다.
                - TRIP_UPDATE_FORBIDDEN: 해당 여행에 대한 수정 권한이 없습니다.
                """,
                    content = @Content),
            @ApiResponse(responseCode = "404", description = """
                보관된 여행 정보를 찾을 수 없습니다.
                - TRIP_NOT_FOUND: 여행을 찾을 수 없습니다.
                """,
                    content = @Content),
            @ApiResponse(responseCode = "409", description = """
                보관된 여행이 아닙니다.
                - TRIP_NOT_STORED: 해당 여행은 보관 상태가 아닙니다.
                """,
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.",
                    content = @Content)
    })
    @PatchMapping("/trips")
    public ResponseEntity<ApiResponseDto<Void>> unstoreTrip(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody List<Long> tripIds
    ) {
        storageService.unstoreTrips(userId, tripIds);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }

    @Operation(summary = "보관된 여행 삭제",
            description = "선택한 보관된(숨긴) 여행들을 삭제합니다. 여행 삭제시 해당 여행의 일기 모두 삭제")
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.TRIP_NOT_FOUND,
            ErrorCode.TRIP_NOT_STORED,
            ErrorCode.TRIP_UPDATE_FORBIDDEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보관된 여행 삭제 성공"),
            @ApiResponse(responseCode = "401", description = """
            인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
            - AUTHENTICATION_FAILED: 인증에 실패했습니다.
            - INVALID_TOKEN: 유효하지 않은 토큰입니다.
            - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
            """, content = @Content),
            @ApiResponse(responseCode = "403", description = """
            권한이 없습니다.
            - TRIP_UPDATE_FORBIDDEN: 해당 여행에 대한 삭제 권한이 없습니다.
            """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
            여행을 찾을 수 없습니다.
            - TRIP_NOT_FOUND: 유효하지 않은 여행 ID입니다.
            """, content = @Content),
            @ApiResponse(responseCode = "409", description = """
            보관된 여행이 아닙니다.
            - TRIP_NOT_STORED: 삭제하려는 여행은 보관 상태가 아닙니다.
            """, content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.",
                    content = @Content)
    })
    @DeleteMapping("/trips")
    public ResponseEntity<ApiResponseDto<Void>> deleteStoredTrips(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody List<Long> tripIds
    ) {
        storageService.deleteStoredTrips(userId, tripIds);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }

    @Operation(summary = "보관된 일기 목록 조회",
            description = "로그인한 사용자의 보관된(숨긴) 일기 목록을 조회합니다.")
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보관된 일기 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED
                - INVALID_TOKEN
                - UNAUTHORIZED_USER
                """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
                사용자 정보를 찾을 수 없습니다:
                - USER_NOT_FOUND
                """, content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.", content = @Content)
    })
    @GetMapping("/diaries")
    public ResponseEntity<ApiResponseDto<StoredDiaryListResponseDto>> getStoredDiaries(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId) {

        StoredDiaryListResponseDto result = storageService.getStoredDiaries(userId);

        return ResponseEntity.ok(
                ApiResponseDto.success(ResponseCode.STORED_DIARY_FOUND_SUCCESS, result)
        );
    }

    @Operation(summary = "보관된 일기 복구",
            description = "보관된(숨긴) 일기를 다시 게시하여 공개 상태로 되돌립니다. ")
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.DIARY_NOT_FOUND,
            ErrorCode.DIARY_NOT_STORED,
            ErrorCode.DIARY_UPDATE_FORBIDDEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일기 복구(게시) 성공"),
            @ApiResponse(responseCode = "401", description = """
            인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
            - AUTHENTICATION_FAILED
            - INVALID_TOKEN
            - UNAUTHORIZED_USER
            """, content = @Content),
            @ApiResponse(responseCode = "403", description = """
            권한이 없습니다.
            - DIARY_UPDATE_FORBIDDEN: 해당 일기에 대한 수정 권한이 없습니다.
            """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
            일기를 찾을 수 없습니다.
            - DIARY_NOT_FOUND: 유효하지 않은 일기 ID입니다.
            """, content = @Content),
            @ApiResponse(responseCode = "409", description = """
            보관된 일기가 아닙니다.
            - DIARY_NOT_STORED: 해당 일기는 보관 상태가 아닙니다.
            """, content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.", content = @Content)
    })
    @PatchMapping("/diaries")
    public ResponseEntity<ApiResponseDto<Void>> restoreStoredDiaries(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody List<Long> diaryIds
    ) {

        storageService.restoreStoredDiaries(userId, diaryIds);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }

    @Operation(summary = "보관된 일기 삭제",
            description = "보관된(숨긴) 일기를 영구 삭제합니다. ")
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.DIARY_NOT_FOUND,
            ErrorCode.DIARY_NOT_STORED,
            ErrorCode.DIARY_UPDATE_FORBIDDEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보관된 일기 삭제 성공"),
            @ApiResponse(responseCode = "401", description = """
            인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
            - AUTHENTICATION_FAILED
            - INVALID_TOKEN
            - UNAUTHORIZED_USER
            """, content = @Content),
            @ApiResponse(responseCode = "403", description = """
            권한이 없습니다.
            - DIARY_UPDATE_FORBIDDEN: 해당 일기에 대한 삭제 권한이 없습니다.
            """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
            일기를 찾을 수 없습니다.
            - DIARY_NOT_FOUND: 유효하지 않은 일기 ID입니다.
            """, content = @Content),
            @ApiResponse(responseCode = "409", description = """
            보관 상태가 아닙니다.
            - DIARY_NOT_STORED: 삭제하려는 일기가 보관 상태가 아닙니다.
            """, content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.", content = @Content)
    })
    @DeleteMapping("/diaries")
    public ResponseEntity<ApiResponseDto<Void>> deleteStoredDiaries(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody List<Long> diaryIds
    ) {
        storageService.deleteStoredDiaries(userId, diaryIds);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }
}
