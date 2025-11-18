package zim.tave.memory.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.dto.StoredTripListResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.StorageService;
import zim.tave.memory.service.TripService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage")
@Tag(name = "Storage", description = "보관된 여행/일기 관리 API")
public class StorageController {

    private final StorageService storageService;

    @Operation(summary = "보관된 여행 목록 조회", description = "로그인한 사용자의 보관된(숨긴) 여행 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보관된 여행 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다."),
            @ApiResponse(responseCode = "404", description = "보관된 여행 정보를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류로 인해 보관된 여행 조회에 실패하였습니다.")
    })
    @GetMapping("/trips")
    public ResponseEntity<ApiResponseDto<StoredTripListResponseDto>> getStoredTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        StoredTripListResponseDto storedTrips = storageService.getStoredTrips(userId);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.STORED_TRIP_FOUND_SUCCESS, storedTrips));
    }
}
