package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zim.tave.memory.dto.WeatherResponseDto;
import zim.tave.memory.dto.swagger.ErrorResponse;
import zim.tave.memory.dto.swagger.WeatherListResponse;
import zim.tave.memory.dto.swagger.WeatherSingleResponse;
import zim.tave.memory.service.WeatherService;

import java.util.List;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
@Tag(name = "Weather", description = "날씨 관련 API")
@SecurityRequirements
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("")
    @Operation(summary = "날씨 목록 조회", description = "사용 가능한 모든 날씨 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "날씨 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = WeatherListResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "서버 오류 예시",
                                    value = """
                                            {
                                              "code": 500,
                                              "message": "서버 오류가 발생했습니다. (errorId: 0cde4715-c546-4af5-ae2e-6c2c324cf112)",
                                              "data": null
                                            }
                                            """
                            )
                    ))
    })
    public ResponseEntity<List<WeatherResponseDto>> getAllWeathers() {
        List<WeatherResponseDto> weathers = weatherService.getAllWeathers();
        return ResponseEntity.ok(weathers);
    }

    @GetMapping("/{weatherId}")
    @Operation(summary = "특정 날씨 조회", description = "ID로 특정 날씨 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "날씨 조회 성공",
                    content = @Content(schema = @Schema(implementation = WeatherSingleResponse.class))),
            @ApiResponse(responseCode = "404", description = "날씨를 찾을 수 없습니다.\n- WEATHER_NOT_FOUND: 날씨를 찾을 수 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "WEATHER_NOT_FOUND 예시",
                                    value = """
                                            {
                                              "code": 404,
                                              "message": "날씨를 찾을 수 없습니다. (errorId: 0cde4715-c546-4af5-ae2e-6c2c324cf113)",
                                              "data": null
                                            }
                                            """
                            )
                    ))
    })
    public WeatherResponseDto getWeatherById(
            @Parameter(description = "날씨 ID", required = true, example = "1")
            @PathVariable Long weatherId) {
        return weatherService.getWeatherById(weatherId);
    }
} 