package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.dto.response.StickerCreateResponseDto;
import zim.tave.memory.dto.response.StickerListResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.service.StickerService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/stickers")
@SecurityRequirement(name = "bearerAuth")
public class StickerController {

    private final StickerService stickerService;

    @Operation(
            summary = "AI 스티커 생성",
            description = "이미지를 업로드하면 AI가 수채화 스티커 스타일로 변환하여 저장합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.FILE_EMPTY,
            ErrorCode.FILE_TOO_LARGE,
            ErrorCode.FILE_TYPE_NOT_ALLOWED,
            ErrorCode.FILE_UPLOAD_FAILED,
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.USER_NOT_FOUND
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "스티커 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "AI 변환 또는 업로드 실패")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<StickerCreateResponseDto>> createSticker(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestPart("image") MultipartFile image
    ) {
        StickerCreateResponseDto response = stickerService.createSticker(userId, image);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(ResponseCode.STICKER_CREATE_SUCCESS, response));
    }

    @Operation(
            summary = "내 스티커 목록 조회",
            description = "로그인한 사용자가 생성한 전체 스티커를 조회합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER
    })
    @GetMapping
    public ResponseEntity<ApiResponseDto<StickerListResponseDto>> getMyStickers(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        StickerListResponseDto response = stickerService.getMyStickers(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDto.success(ResponseCode.SUCCESS, response));
    }
}
