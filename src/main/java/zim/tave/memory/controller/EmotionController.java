package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.domain.Emotion;
import zim.tave.memory.dto.swagger.EmotionListResponse;
import zim.tave.memory.dto.swagger.ErrorResponse;
import zim.tave.memory.service.EmotionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emotions")
@Tag(name = "Emotion-Controller", description = "감정(Emotion) 리스트 조회")
@SecurityRequirements
public class EmotionController {
    private final EmotionService emotionService;

    @Operation(summary = "감정 리스트 조회", description = "모든 감정(Emotion)과 색상 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = EmotionListResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류입니다.",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            name = "서버 오류 예시",
                            value = """
                                    {
                                      "code": 500,
                                      "message": "서버 오류가 발생했습니다. (errorId: 0cde4715-c546-4af5-ae2e-6c2c324cf115)",
                                      "data": null
                                    }
                                    """
                    )
            ))
    })
    @GetMapping("")
    public ResponseEntity<List<Emotion>> getAllEmotions() {
        List<Emotion> emotions = emotionService.getAllEmotions();
        return ResponseEntity.ok(emotions);
    }
}
