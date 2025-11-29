package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.dto.request.BoardCreateRequestDto;
import zim.tave.memory.dto.response.BoardCreateResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.service.BoardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    @Operation(
            summary = "보드 생성",
            description = "선택한 테마와 제목을 이용하여 새로운 보드를 생성합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.BOARD_REQUIRED_FIELDS_MISSING,
            ErrorCode.BOARD_THEME_NOT_FOUND,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 생성 성공"),
            @ApiResponse(responseCode = "400", description = """
                잘못된 요청입니다. 다음 에러 코드가 발생할 수 있습니다:
                - BOARD_REQUIRED_FIELDS_MISSING: 필수 입력값 누락
                """, content = @Content),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다.
                - AUTHENTICATION_FAILED
                - INVALID_TOKEN
                - UNAUTHORIZED_USER
                """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
                리소스를 찾을 수 없습니다:
                - BOARD_THEME_NOT_FOUND: 존재하지 않는 보드 테마
                - USER_NOT_FOUND: 존재하지 않는 사용자
                """, content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<BoardCreateResponseDto>> createBoard(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody BoardCreateRequestDto requestDto) {

        BoardCreateResponseDto response = boardService.createBoard(userId, requestDto);

        return ResponseEntity.ok(
                ApiResponseDto.success(ResponseCode.BOARD_CREATE_SUCCESS, response)
        );
    }
}
