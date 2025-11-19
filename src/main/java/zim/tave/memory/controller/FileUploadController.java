package zim.tave.memory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import zim.tave.memory.config.FileUploadProperties;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.service.S3Uploader;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final S3Uploader s3Uploader;
    private final FileUploadProperties fileUploadProperties;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponseDto<String>> uploadFile(@RequestParam("file") MultipartFile file,
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
