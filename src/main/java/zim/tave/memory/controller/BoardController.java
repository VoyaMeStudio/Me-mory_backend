package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.dto.request.BoardCreateRequestDto;
import zim.tave.memory.dto.request.BoardStickerAddRequestDto;
import zim.tave.memory.dto.request.BoardUpdateRequestDto;
import zim.tave.memory.dto.request.StickerPositionUpdateRequestDto;
import zim.tave.memory.dto.response.*;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.service.BoardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
@SecurityRequirement(name = "bearerAuth")
public class BoardController {

    private final BoardService boardService;

    @Operation(
            summary = "보드 생성",
            description = "선택한 보드 테마와 제목을 이용해 새로운 보드를 생성합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.BOARD_REQUIRED_FIELDS_MISSING,
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.BOARD_THEME_NOT_FOUND,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.USER_NOT_FOUND
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 생성 성공"),

            @ApiResponse(responseCode = "400", description = """
                잘못된 요청입니다. 다음 에러 코드가 발생할 수 있습니다:
                - BOARD_REQUIRED_FIELDS_MISSING: 필수 입력값 누락
                """, content = @Content),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
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
            description = "로그인한 사용자가 생성한 전체 보드를 조회합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.BOARD_UPDATE_INTERNAL_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED
                - INVALID_TOKEN
                - UNAUTHORIZED_USER
                """, content = @Content),
            @ApiResponse(responseCode = "500", description = """
                서버 오류가 발생했습니다.
                """, content = @Content)
    })
    @GetMapping
    public ResponseEntity<ApiResponseDto<BoardListResponseDto>> getBoards(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        BoardListResponseDto response = boardService.getUserBoards(userId);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, response));
    }

    @Operation(
            summary = "특정 보드 상세정보 조회",
            description = "보드의 기본 정보와 스티커 정보를 모두 조회합니다."
    )
    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponseDto<BoardDetailResponseDto>> getBoardDetail(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long boardId) {

        BoardDetailResponseDto response = boardService.getBoardDetail(userId, boardId);

        return ResponseEntity.ok(
                ApiResponseDto.success(ResponseCode.BOARD_FETCH_SUCCESS, response)
        );
    }

    @Operation(
            summary = "보드 삭제",
            description = "지정된 보드를 삭제합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.BOARD_DELETE_FORBIDDEN,
            ErrorCode.BOARD_NOT_FOUND
    })
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "보드 삭제 성공"),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED
                - INVALID_TOKEN
                - UNAUTHORIZED_USER
                """, content = @Content),
            @ApiResponse(responseCode = "403", description = """
                권한 없음:
                - BOARD_DELETE_FORBIDDEN: 해당 보드를 삭제할 권한이 없습니다.
                """, content = @Content),
            @ApiResponse(responseCode = "404", description = """
                리소스를 찾을 수 없습니다:
                - BOARD_NOT_FOUND: 존재하지 않는 보드입니다.
                """, content = @Content),
            @ApiResponse(responseCode = "500", description = """
                서버 오류가 발생했습니다.
                """, content = @Content)
    })
    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBoard(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long boardId
    ) {

        boardService.deleteBoard(userId, boardId);

        return ResponseEntity.ok(
                ApiResponseDto.success(ResponseCode.BOARD_DELETE_SUCCESS, null)
        );
    }

    @Operation(
            summary = "보드에 스티커 추가",
            description = "특정 보드에 스티커를 추가합니다."
    )
    @PatchMapping("/{boardId}/stickers")
    public ResponseEntity<ApiResponseDto<BoardStickerAddResponseDto>> addSticker(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long boardId,
            @RequestBody BoardStickerAddRequestDto requestDto) {

        BoardStickerAddResponseDto response = boardService.addSticker(userId, boardId, requestDto);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, response));
    }

    @Operation(
            summary = "보드에서 스티커 위치/각도 수정",
            description = "특정 보드에 있는 특정 스티커의 위치/각도를 수정합니다."
    )
    @PatchMapping("/{boardId}/stickers/{boardStickerId}")
    public ResponseEntity<ApiResponseDto<BoardStickerUpdateResponseDto>> updateSticker(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long boardId,
            @PathVariable Long boardStickerId,
            @RequestBody StickerPositionUpdateRequestDto requestDto
    ) {
        BoardStickerUpdateResponseDto response = boardService.updateSticker(
                userId, boardId, boardStickerId, requestDto);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, response));
    }

    @Operation(
            summary = "보드에서 스티커 삭제",
            description = "특정 보드에 있는 특정 스티커를 삭제합니다."
    )
    @DeleteMapping("/{boardId}/stickers/{boardStickerId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBoardSticker(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long boardId,
            @PathVariable Long boardStickerId
    ) {
        boardService.deleteStickerFromBoard(userId, boardId, boardStickerId);
        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, null));
    }

}
