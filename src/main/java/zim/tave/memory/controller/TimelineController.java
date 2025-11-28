package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.dto.request.CreatePastTripRequest;
import zim.tave.memory.dto.response.TimelineResponseDto;
import zim.tave.memory.dto.response.TripResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.service.TimelineService;
import zim.tave.memory.service.TripService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
@Tag(name = "Timeline", description = "타임라인 조회 API")
@SecurityRequirement(name = "bearerAuth")
public class TimelineController {

    private final TimelineService timelineService;
    private final TripService tripService;

    @GetMapping("/timeline")
    @Operation(summary = "타임라인 조회", description = "로그인한 사용자의 과거 및 현재 여행 타임라인을 조회합니다. 보관함에 숨긴 여행은 결과에서 제외됩니다.")
    @ApiErrorCodeExamples({ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "타임라인 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                    - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                    - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                    """,
                    content = @Content)
    })
    public ResponseEntity<ApiResponseDto<TimelineResponseDto>> getTimeline(
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        TimelineResponseDto responseDto = timelineService.getTimeline(userId);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, responseDto));
    }

    @PostMapping("/timeline/past")
    @Operation(summary = "타임라인에서 과거 여행 추가", description = "타임라인 화면에서 과거에 다녀온 여행을 등록합니다.")
    @ApiErrorCodeExamples({ErrorCode.TRIP_NAME_REQUIRED, ErrorCode.TRIP_NAME_TOO_LONG, ErrorCode.TRIP_DESCRIPTION_TOO_LONG, ErrorCode.MISSING_REQUIRED_FIELDS, ErrorCode.VALIDATION_ERROR, ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.TRIP_THEME_NOT_FOUND})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "과거 여행 생성 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청 데이터입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - TRIP_NAME_REQUIRED: 여행명은 필수입니다.
                    - TRIP_NAME_TOO_LONG: 여행명은 최대 14자입니다.
                    - TRIP_DESCRIPTION_TOO_LONG: 여행 설명은 최대 56자입니다.
                    - MISSING_REQUIRED_FIELDS: 값을 입력해주세요.
                    - VALIDATION_ERROR: 요청 값이 올바르지 않습니다.
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                    - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                    - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없습니다. 다음 에러 코드가 발생할 수 있습니다:\n- TRIP_THEME_NOT_FOUND: 여행 테마를 찾을 수 없습니다.",
                    content = @Content)
    })
    public ResponseEntity<ApiResponseDto<TripResponseDto>> createPastTripFromTimeline(
            @RequestBody CreatePastTripRequest request,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        TripResponseDto dto = tripService.createPastTrip(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(ResponseCode.CREATED, dto));
    }
}

