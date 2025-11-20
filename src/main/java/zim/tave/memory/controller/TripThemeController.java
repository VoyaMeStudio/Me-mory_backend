package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.domain.TripTheme;
import zim.tave.memory.dto.TripThemeResponseDto;
import zim.tave.memory.dto.swagger.ErrorResponse;
import zim.tave.memory.dto.swagger.TripThemeListResponse;
import zim.tave.memory.repository.TripThemeRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
@Tag(name = "TripTheme", description = "여행 테마 관리 API")
public class TripThemeController {

    private final TripThemeRepository tripThemeRepository;

    @GetMapping
    @Operation(summary = "테마 목록 조회", description = "모든 여행 테마 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "테마 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = TripThemeListResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "서버 오류 예시",
                                    value = """
                                            {
                                              "code": 500,
                                              "message": "서버 오류가 발생했습니다. (errorId: 0cde4715-c546-4af5-ae2e-6c2c324cf114)",
                                              "data": null
                                            }
                                            """
                            )
                    ))
    })
    public ResponseEntity<List<TripThemeResponseDto>> getAllThemes() {
        List<TripTheme> themes = tripThemeRepository.findAllByOrderById();
        List<TripThemeResponseDto> response = themes.stream()
                .map(TripThemeResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
