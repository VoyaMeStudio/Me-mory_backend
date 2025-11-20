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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.dto.response.MyPageResponseDto;
import zim.tave.memory.dto.response.VisitedCountryListResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.MyPageService;
import zim.tave.memory.service.VisitedCountryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
@Tag(name = "MyPage-Controller", description = "마이페이지 조회")
@SecurityRequirement(name = "bearerAuth")
public class MyPageController {

    private final MyPageService myPageService;
    private final VisitedCountryService visitedCountryService;

    @Operation(summary = "마이페이지 조회", description = "JWT 토큰 이용하여 마이페이지 조회")
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INCOMPLETE_USER_INFO
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "마이페이지 조회 성공"),
            @ApiResponse(responseCode = "400", description = """
                잘못된 요청입니다. 다음 에러 코드가 발생할 수 있습니다:
                - INCOMPLETE_USER_INFO: 필수 정보가 입력되지 않은 사용자입니다.
                """, content = @Content),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED
                - INVALID_TOKEN
                - UNAUTHORIZED_USER
                """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
                리소스를 찾을 수 없습니다:
                - USER_NOT_FOUND: 사용자를 찾을 수 없습니다.
                """, content = @Content)
    })
    @GetMapping
    public ResponseEntity<ApiResponseDto<MyPageResponseDto>> getMyPage(
            @AuthenticationPrincipal(expression = "userId") Long userId) {

        MyPageResponseDto responseDto = myPageService.getMyPage(userId);

        return ResponseEntity.ok(
                ApiResponseDto.success(ResponseCode.SUCCESS, responseDto)
        );
    }

    @Operation(summary = "방문 국가 목록 조회", description = "현재 로그인한 사용자의 방문 국가 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방문 국가 조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "방문한 국가 정보를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류로 인해 방문 국가 조회에 실패하였습니다.")
    })
    @GetMapping("/visitedcountries")
    public ResponseEntity<ApiResponseDto<VisitedCountryListResponseDto>> getVisitedCountries(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        VisitedCountryListResponseDto response = visitedCountryService.getVisitedCountryList(userId);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.VISITED_COUNTRIES_FETCH_SUCCESS, response));
    }

    /*
    //에러 테스트 코드
    @GetMapping("/test-error")
    public ResponseEntity<ApiResponseDto<MyPageResponseDto>> testError(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        MyPageResponseDto responseDto = myPageService.getErrorExample(userId);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, responseDto));
    }
     */
}
