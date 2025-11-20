package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zim.tave.memory.domain.Country;
import zim.tave.memory.dto.response.CountrySearchResponseDto;
import zim.tave.memory.dto.request.RegisterVisitedCountryRequestDto;
import zim.tave.memory.dto.response.VisitedCountryResponseDto;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.CountryService;
import zim.tave.memory.service.VisitedCountryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Country", description = "국가 조회 및 방문 국가 관리")
public class CountryController {

    private final VisitedCountryService visitedCountryService;
    private final CountryService countryService;

    @Operation(summary = "국가 검색", description = "공통 마스터 데이터에서 국가 목록을 키워드로 검색합니다.")
    @SecurityRequirements
    @ApiErrorCodeExamples({ErrorCode.VALIDATION_ERROR, ErrorCode.INTERNAL_SERVER_ERROR})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. 다음 에러 코드가 발생할 수 있습니다:\n- VALIDATION_ERROR: 요청 값이 올바르지 않습니다.",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류입니다.",
            content = @Content)
    })
    @GetMapping("/countries")
    public ResponseEntity<ApiResponseDto<List<CountrySearchResponseDto>>> searchCountries(
        @Parameter(description = "검색할 국가 이름(한글)", example = "대한")
        @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        List<Country> countries = countryService.searchCountriesByName(keyword);
        List<CountrySearchResponseDto> result = countries.stream()
            .map(CountrySearchResponseDto::from)
            .toList();
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, result));
    }

    @Operation(summary = "방문 국가 목록 조회", description = "JWT 토큰으로 인증된 사용자의 방문 국가 및 감정 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiErrorCodeExamples({ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.ACCESS_DENIED, ErrorCode.INTERNAL_SERVER_ERROR})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                """,
            content = @Content),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다.\n- ACCESS_DENIED: 접근 권한이 없습니다.",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류입니다.",
            content = @Content)
    })
    @GetMapping("/users/me/visited-countries")
    public ResponseEntity<ApiResponseDto<List<VisitedCountryResponseDto>>> getVisitedCountries(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        List<VisitedCountryResponseDto> visitedCountries = visitedCountryService.getVisitedCountries(userId);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, visitedCountries));
    }

    @Operation(summary = "방문 국가 등록 또는 업데이트", description = "사용자의 방문 국가를 등록하거나 감정 정보를 업데이트합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiErrorCodeExamples({ErrorCode.INVALID_COUNTRY_CODE, ErrorCode.VALIDATION_ERROR, ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.COUNTRY_NOT_FOUND, ErrorCode.USER_NOT_FOUND, ErrorCode.EMOTION_NOT_FOUND, ErrorCode.DEFAULT_EMOTION_NOT_CONFIGURED})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "등록 또는 업데이트 성공"),
        @ApiResponse(responseCode = "400", description = """
                잘못된 요청입니다. 다음 에러 코드가 발생할 수 있습니다:
                - INVALID_COUNTRY_CODE: 올바르지 않은 국가 코드입니다.
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
        @ApiResponse(responseCode = "404", description = """
                리소스를 찾을 수 없습니다. 다음 에러 코드가 발생할 수 있습니다:
                - COUNTRY_NOT_FOUND: 국가를 찾을 수 없습니다.
                - USER_NOT_FOUND: 사용자를 찾을 수 없습니다.
                - EMOTION_NOT_FOUND: 감정을 찾을 수 없습니다.
                """,
            content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류입니다. 다음 에러 코드가 발생할 수 있습니다:\n- DEFAULT_EMOTION_NOT_CONFIGURED: 기본 감정 정보가 설정되어 있지 않습니다.",
            content = @Content)
    })
    @PostMapping("/users/me/visited-countries")
    public ResponseEntity<ApiResponseDto<VisitedCountryResponseDto>> registerVisitedCountry(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "방문 국가 등록 요청 DTO (countryCode, emotionId)",
            required = true,
            content = @Content(schema = @Schema(implementation = RegisterVisitedCountryRequestDto.class))
        )
        @RequestBody RegisterVisitedCountryRequestDto requestDto
    ) {
        Long userId = userDetails.getUserId();
        VisitedCountryResponseDto responseDto = visitedCountryService.registerVisitedCountry(
            userId,
            requestDto.getCountryCode(),
            requestDto.getEmotionId()
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponseDto.success(ResponseCode.CREATED, responseDto));
    }

    @Operation(summary = "방문 국가 삭제", description = "사용자의 방문 국가를 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiErrorCodeExamples({ErrorCode.INVALID_COUNTRY_CODE, ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_TOKEN, ErrorCode.UNAUTHORIZED_USER, ErrorCode.ACCESS_DENIED, ErrorCode.VISITED_COUNTRY_NOT_FOUND, ErrorCode.INTERNAL_SERVER_ERROR})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. 다음 에러 코드가 발생할 수 있습니다:\n- INVALID_COUNTRY_CODE: 올바르지 않은 국가 코드입니다.",
            content = @Content),
        @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED: 인증에 실패했습니다.
                - INVALID_TOKEN: 유효하지 않은 토큰입니다.
                - UNAUTHORIZED_USER: 인증되지 않은 사용자입니다.
                """,
            content = @Content),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다.\n- ACCESS_DENIED: 접근 권한이 없습니다.",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "방문 국가를 찾을 수 없습니다.\n- VISITED_COUNTRY_NOT_FOUND: 방문 국가 기록을 찾을 수 없습니다.",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류입니다.",
            content = @Content)
    })
    @DeleteMapping("/users/me/visited-countries/{countryCode}")
    public ResponseEntity<ApiResponseDto<Void>> deleteVisitedCountry(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(description = "삭제할 국가 코드", example = "KR")
        @PathVariable String countryCode
    ) {
        Long userId = userDetails.getUserId();
        visitedCountryService.deleteVisitedCountry(userId, countryCode);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }
}
