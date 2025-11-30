package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.dto.request.BoardCreateRequestDto;
import zim.tave.memory.dto.request.BoardUpdateRequestDto;
import zim.tave.memory.dto.response.BoardCreateResponseDto;
import zim.tave.memory.dto.response.BoardListResponseDto;
import zim.tave.memory.dto.response.BoardUpdateResponseDto;
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

    @Operation(
            summary = "보드 목록 조회",
            description = "현재 로그인한 사용자가 생성한 모든 보드 목록을 조회합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BoardListResponseDto.class))),
            @ApiResponse(responseCode = "401", description = """
            인증 실패입니다.
            - AUTHENTICATION_FAILED
            - INVALID_TOKEN
            - UNAUTHORIZED_USER
            """, content = @Content),
            @ApiResponse(responseCode = "500", description = """
            서버 오류입니다.
            보드 목록 조회 중 문제가 발생했습니다.
            """, content = @Content)
    })
    @GetMapping
    public ResponseEntity<ApiResponseDto<BoardListResponseDto>> getBoards(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        BoardListResponseDto response = boardService.getUserBoards(userId);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, response));
    }

    @PatchMapping("/{boardId}")
    @Operation(summary = "보드 수정 (스티커 전체 업데이트)",
            description = "해당 보드의 기존 스티커를 모두 삭제하고, 새로운 스티커 전체 리스트로 교체합니다.")
    @ApiErrorCodeExamples({
            ErrorCode.BOARD_NOT_FOUND,
            ErrorCode.BOARD_STICKER_NOT_FOUND,
            ErrorCode.BOARD_UPDATE_FORBIDDEN,
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 수정 성공"),
            @ApiResponse(responseCode = "400", description = """
                잘못된 요청입니다:
                - INVALID_STICKER_ID: 잘못된 스티커 ID 포함
                """, content = @Content),
            @ApiResponse(responseCode = "403", description = """
                권한 없음:
                - BOARD_UPDATE_FORBIDDEN
                """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
                리소스 없음:
                - BOARD_NOT_FOUND: 존재하지 않는 보드
                """, content = @Content)
    })
    public ResponseEntity<ApiResponseDto<BoardUpdateResponseDto>> updateBoard(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long boardId,
            @RequestBody BoardUpdateRequestDto requestDto) {

        BoardUpdateResponseDto response = boardService.updateBoardStickers(userId, boardId, requestDto);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.BOARD_UPDATE_SUCCESS, response));
    }
}
