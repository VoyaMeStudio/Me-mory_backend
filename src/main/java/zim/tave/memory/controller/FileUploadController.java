package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import zim.tave.memory.config.FileUploadProperties;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.service.S3Uploader;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Tag(name = "FileUpload", description = "파일 업로드 API")
@SecurityRequirements
public class FileUploadController {

    private final S3Uploader s3Uploader;
    private final FileUploadProperties fileUploadProperties;

    @PostMapping("/upload")
    @Operation(summary = "파일 업로드", description = "이미지 파일을 S3에 업로드하고 URL을 반환합니다. 현재는 'images' 타입만 지원합니다.")
    @ApiErrorCodeExamples({ErrorCode.FILE_EMPTY, ErrorCode.FILE_TOO_LARGE, ErrorCode.FILE_TYPE_NOT_ALLOWED, ErrorCode.INVALID_REQUEST, ErrorCode.FILE_UPLOAD_FAILED})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 업로드 성공",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청입니다. 다음 에러 코드가 발생할 수 있습니다:
                    - FILE_EMPTY: 업로드할 파일이 비어있습니다.
                    - FILE_TOO_LARGE: 파일 크기가 허용 범위를 초과했습니다.
                    - FILE_TYPE_NOT_ALLOWED: 지원하지 않는 파일 형식입니다.
                    - INVALID_REQUEST: 타입이 잘못되었습니다. (현재 'images'만 지원)
                    """,
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다. 다음 에러 코드가 발생할 수 있습니다:\n- FILE_UPLOAD_FAILED: 파일 업로드에 실패했습니다.",
                    content = @Content)
    })
    public ResponseEntity<ApiResponseDto<String>> uploadFile(
            @Parameter(description = "업로드할 파일", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "파일 타입 (현재 'images'만 지원)", required = true, example = "images")
            @RequestParam("type") String type) {
        validateFile(file, type);

        try {
            String url = s3Uploader.upload(file, type);
            return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, url));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void validateFile(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > fileUploadProperties.getMaxFileSize()) {
            throw new CustomException(ErrorCode.FILE_TOO_LARGE);
        }

        if (!"images".equals(type)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        String fileExtension = extractExtension(fileName);

        boolean validContentType = contentType != null
                && fileUploadProperties.getAllowedImageTypes().contains(contentType.toLowerCase());
        boolean validExtension = fileUploadProperties.getAllowedImageExtensions().contains(fileExtension);

        if (!validContentType && !validExtension) {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }
}
