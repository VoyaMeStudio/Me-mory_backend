package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "File", description = "파일 다운로드 API")
public class FileController {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @GetMapping("/api/files")
    @Operation(summary = "파일 다운로드", description = "S3에 저장된 파일을 다운로드합니다. 이미지 파일의 경우 브라우저에서 직접 표시됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 다운로드 성공",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    public ResponseEntity<InputStreamResource> getFile(
            @Parameter(description = "S3 파일 키 (경로 포함)", required = true, example = "images/2024/01/01/abc123.jpg")
            @RequestParam String key) {
        try {
            log.info("FileController - Requested key: {}", key);
            log.info("FileController - Bucket: {}", bucket);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            GetObjectResponse response = s3Object.response();

            HttpHeaders headers = new HttpHeaders();
            String contentType = response.contentType();
            String fileName = key.substring(key.lastIndexOf("/") + 1);

            // HEIF 파일의 경우 브라우저 호환성을 위해 적절한 Content-Type 설정
            if (key.toLowerCase().contains(".heic") ||
                (contentType != null && contentType.contains("heif"))) {
                headers.setContentType(MediaType.parseMediaType("image/heic"));
                // HEIF는 브라우저 지원이 제한적이므로 다운로드 유도
                headers.add("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            } else if (contentType != null) {
                headers.setContentType(MediaType.parseMediaType(contentType));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            headers.setContentLength(response.contentLength());

            log.info("FileController - File found successfully: {}", key);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(s3Object));

        } catch (Exception e) {
            log.error("FileController - Error while fetching key={}", key, e);
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
