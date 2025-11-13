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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import zim.tave.memory.domain.Country;
import zim.tave.memory.dto.CountrySearchResponseDto;
import zim.tave.memory.dto.RegisterVisitedCountryRequestDto;
import zim.tave.memory.dto.VisitedCountryResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "검색 성공",
            content = @Content(schema = @Schema(implementation = CountrySearchResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content())
    })
    @GetMapping("/countries")
    public ResponseEntity<ApiResponseDto<List<CountrySearchResponseDto>>> searchCountries(
        @Parameter(description = "검색할 국가 이름(한글)", example = "대한")
        @RequestParam String keyword
    ) {
        List<Country> countries = countryService.searchCountriesByName(keyword);
        List<CountrySearchResponseDto> result = countries.stream()
            .map(c -> new CountrySearchResponseDto(c.getCountryCode(), c.getCountryName(), c.getEmoji()))
            .toList();
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, result));
    }

    @Operation(summary = "방문 국가 목록 조회", description = "JWT 토큰으로 인증된 사용자의 방문 국가 및 감정 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = VisitedCountryResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content()),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content())
    })
    @GetMapping("/users/me/visited-countries")
    public ResponseEntity<ApiResponseDto<List<VisitedCountryResponseDto>>> getVisitedCountries(
        @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        List<VisitedCountryResponseDto> visitedCountries = visitedCountryService.getVisitedCountries(userId)
                .stream()
                .map(VisitedCountryResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, visitedCountries));
    }

    @Operation(summary = "방문 국가 등록 또는 업데이트", description = "사용자의 방문 국가를 등록하거나 감정 정보를 업데이트합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "등록 또는 업데이트 성공",
            content = @Content(schema = @Schema(implementation = VisitedCountryResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content()),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content()),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content())
    })
    @PostMapping("/users/me/visited-countries")
    public ResponseEntity<ApiResponseDto<VisitedCountryResponseDto>> registerVisitedCountry(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "방문 국가 등록 요청 DTO (countryCode, emotionId)",
            required = true,
            content = @Content(schema = @Schema(implementation = RegisterVisitedCountryRequestDto.class))
        )
        @RequestBody RegisterVisitedCountryRequestDto requestDto
    ) {
        Long userId = userDetails.getUserId();
        VisitedCountryResponseDto responseDto = VisitedCountryResponseDto.from(
            visitedCountryService.registerVisitedCountry(
                userId,
                requestDto.getCountryCode(),
                requestDto.getEmotionId()
            )
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponseDto.success(ResponseCode.CREATED, responseDto));
    }

    @Operation(summary = "방문 국가 삭제", description = "사용자의 방문 국가를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content()),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content()),
        @ApiResponse(responseCode = "404", description = "방문 국가를 찾을 수 없음", content = @Content()),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content())
    })
    @DeleteMapping("/users/me/visited-countries/{countryCode}")
    public ResponseEntity<ApiResponseDto<Void>> deleteVisitedCountry(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(description = "삭제할 국가 코드", example = "KR")
        @PathVariable String countryCode
    ) {
        Long userId = userDetails.getUserId();
        visitedCountryService.deleteVisitedCountry(userId, countryCode);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }
}