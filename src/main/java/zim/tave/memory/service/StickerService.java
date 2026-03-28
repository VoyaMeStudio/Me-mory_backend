package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import zim.tave.memory.config.FileUploadProperties;
import zim.tave.memory.domain.Sticker;
import zim.tave.memory.domain.User;
import zim.tave.memory.domain.UserSticker;
import zim.tave.memory.dto.response.StickerCreateResponseDto;
import zim.tave.memory.dto.response.StickerListResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.StickerRepository;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.repository.UserStickerRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StickerService {

    private final StickerRepository stickerRepository;
    private final UserStickerRepository userStickerRepository;
    private final S3Client s3Client;
    private final RestTemplate restTemplate;
    private final FileUploadProperties fileUploadProperties;
    private final UserRepository userRepository;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.base-url}")
    private String s3BaseUrl;

    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L; // 10MB

    @Transactional
    public StickerCreateResponseDto createSticker(Long userId, MultipartFile file) {

        // 1. 파일 유효성 검사
        validateFile(file);

        // 2. Python AI 서버로 이미지 전송 → 변환된 PNG 바이트 수신
        byte[] transformedBytes = callAiServer(file);

        // 3. S3 업로드
        String s3Key = "stickers/user-" + userId + "/" + UUID.randomUUID() + ".png";
        uploadToS3(s3Key, transformedBytes);
        String imageUrl = s3BaseUrl + "/" + s3Key;

        // 4. Sticker 공통 엔티티 저장 (name은 원본 파일명 기반)
        String stickerName = extractBaseName(file.getOriginalFilename());
        Sticker sticker = Sticker.builder()
                .name(stickerName)
                .imageUrl(imageUrl)
                .build();
        Sticker savedSticker = stickerRepository.save(sticker);

        // 5. UserSticker 연결 저장 (유저-스티커 소유 관계)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserSticker userSticker = UserSticker.builder()
                .user(user)
                .sticker(savedSticker)
                .build();
        userStickerRepository.save(userSticker);

        return StickerCreateResponseDto.from(savedSticker);
    }

    public StickerListResponseDto getMyStickers(Long userId) {
        List<UserSticker> userStickers = userStickerRepository.findByUserId(userId);
        List<Sticker> stickers = userStickers.stream()
                .map(UserSticker::getSticker)
                .toList();
        return StickerListResponseDto.from(stickers);
    }

    // ── private helpers ──────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > fileUploadProperties.getMaxFileSize()) {
            throw new CustomException(ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !fileUploadProperties.getAllowedImageTypes().contains(contentType)) {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);

        if (!fileUploadProperties.getAllowedImageExtensions().contains(extension)) {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private byte[] callAiServer(MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    aiServerUrl + "/transform",
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
            }
            return response.getBody();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 서버 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void uploadToS3(String key, byte[] data) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("image/png")
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(data));
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String extractBaseName(String originalFilename) {
        if (originalFilename == null) return "sticker";
        int dot = originalFilename.lastIndexOf('.');
        return dot > 0 ? originalFilename.substring(0, dot) : originalFilename;
    }

}
