package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import zim.tave.memory.dto.TimelineResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.service.TimelineService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
@Tag(name = "Timeline", description = "타임라인 조회 API")
@SecurityRequirement(name = "bearerAuth")
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/timeline")
    @Operation(summary = "타임라인 조회", description = "로그인한 사용자의 과거 및 현재 여행 타임라인을 조회합니다.")
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
}

