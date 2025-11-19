package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zim.tave.memory.dto.*;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.service.DiaryService;
import zim.tave.memory.service.TripService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Trip", description = "여행 관리 API")
public class TripController {

    private final TripService tripService;
    private final DiaryService diaryService;

    @PostMapping("/users/me/trips")
    @Operation(summary = "여행 생성", description = "새로운 여행을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "여행 생성 성공",
                    content = @Content(schema = @Schema(implementation = TripResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 누락 또는 만료)", content = @Content()),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    })
    public ResponseEntity<ApiResponseDto<TripResponseDto>> createTrip(
            @RequestBody CreateTripRequest request,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        TripResponseDto dto = tripService.createTrip(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(ResponseCode.CREATED, dto));
    }

    @PatchMapping("/users/me/trips/{tripId}")
    @Operation(summary = "여행 수정", description = "기존 여행 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "여행 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 누락 또는 만료)", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 아님)", content = @Content)
    })
    public ResponseEntity<ApiResponseDto<Void>> updateTrip(
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId,
            @RequestBody UpdateTripRequest request,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        tripService.updateTrip(tripId, request, userId);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }

    @GetMapping("/users/me/trips")
    @Operation(summary = "사용자 여행 전체 목록 조회", description = "JWT 인증 후 해당 사용자의 모든 여행을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "여행 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = TripResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 누락 또는 만료)", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 사용자의 여행 없음", content = @Content)
    })
    public ResponseEntity<ApiResponseDto<List<TripResponseDto>>> getAllTrips(
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        List<TripResponseDto> dtos = tripService.getTripsByUserId(userId);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, dtos));
    }

    @GetMapping("/users/me/trips/{tripId}")
    @Operation(summary = "여행 상세 조회", description = "특정 여행의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "여행 조회 성공",
                    content = @Content(schema = @Schema(implementation = TripResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 누락 또는 만료)", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 아님)", content = @Content)
    })
    public ResponseEntity<ApiResponseDto<TripResponseDto>> getTrip(
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
		TripResponseDto dto = tripService.getTripDtoWithOwnershipCheck(tripId, userId);
		return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, dto));
    }

    @GetMapping("/users/me/trips/{tripId}/representative-images")
    @Operation(summary = "여행에 속한 다이어리들의 대표사진 목록 조회", description = "특정 여행에 속한 모든 다이어리들의 대표사진을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대표사진 조회 성공",
                    content = @Content(schema = @Schema(implementation = TripRepresentativeImageDto.class))),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 누락 또는 만료)", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 아님)", content = @Content)
    })
    public ResponseEntity<ApiResponseDto<List<TripRepresentativeImageDto>>> getRepresentativeImages(
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
		tripService.validateOwnership(tripId, userId);

        List<TripRepresentativeImageDto> representativeImages =
                diaryService.getRepresentativeImagesByTripId(tripId);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, representativeImages));
    }

    @PutMapping("/users/me/trips/{tripId}/representative-image")
    @Operation(summary = "여행 대표사진 설정", description = "여행의 대표사진을 설정합니다. 해당 여행에 속한 다이어리의 대표사진 중에서 선택할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "여행 대표사진 설정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "404", description = "여행 또는 이미지를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 누락 또는 만료)", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 아님)", content = @Content)
    })
    public ResponseEntity<ApiResponseDto<Void>> updateTripRepresentativeImage(
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId,
            @RequestBody UpdateRepresentativeImageRequest request,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
		tripService.updateTripRepresentativeImageWithOwnershipCheck(tripId, request.getImageId(), userId);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }

    @PostMapping("/users/me/trips/past")
    @Operation(summary = "과거 여행 추가", description = "과거에 다녀온 여행을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "과거 여행 생성 성공",
                    content = @Content(schema = @Schema(implementation = TripResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    })
    public ResponseEntity<ApiResponseDto<TripResponseDto>> createPastTrip(
            @RequestBody CreatePastTripRequest request,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        TripResponseDto dto = tripService.createPastTrip(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(ResponseCode.CREATED, dto));
    }

    @PatchMapping("/users/me/trips/{tripId}/store")
    @Operation(summary = "여행 보관 상태 변경", description = "여행을 보관하거나 보관 해제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보관 상태 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<ApiResponseDto<Void>> storeTrip(
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId,
            @RequestParam boolean isStored,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        tripService.storeTrip(tripId, userId, isStored);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }
}
