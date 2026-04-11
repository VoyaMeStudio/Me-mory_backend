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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StickerService {

    private final StickerRepository stickerRepository;
    private final UserStickerRepository userStickerRepository;
    private final RestTemplate restTemplate;
    private final FileUploadProperties fileUploadProperties;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Transactional
    public StickerCreateResponseDto createSticker(Long userId, MultipartFile file) {

        // 1. 파일 유효성 검사 (yml 기반)
        validateFile(file);

        // 2. 사용자 조회 (userId → User 엔티티)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. Python AI 서버 호출 → 변환된 PNG 바이트 수신
        byte[] transformedBytes = callAiServer(file);

        // 4. 변환된 파일을 MultipartFile처럼 감싸기 (S3Uploader 사용 위해)
        MultipartFile transformedFile = new ByteArrayMultipartFile(
                transformedBytes,
                "sticker.png",
                "image/png",
                "sticker.png"
        );

        // 5. S3 업로드 (S3Uploader 사용)
        String imageUrl;
        try {
            imageUrl = s3Uploader.upload(
                    transformedFile,
                    "stickers/user-" + userId
            );
        } catch (IOException e) {
            log.error("S3 업로드 실패", e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        // 6. Sticker 저장
        String stickerName = extractBaseName(file.getOriginalFilename());

        Sticker sticker = Sticker.builder()
                .name(stickerName)
                .imageUrl(imageUrl)
                .build();

        Sticker savedSticker = stickerRepository.save(sticker);

        // 7. UserSticker 저장 (userId → user)
        UserSticker userSticker = UserSticker.builder()
                .user(user)
                .sticker(savedSticker)
                .build();

        userStickerRepository.save(userSticker);

        return StickerCreateResponseDto.from(savedSticker);
    }

    public class ByteArrayMultipartFile implements MultipartFile {

        private final byte[] data;
        private final String filename;
        private final String contentType;
        private final String originalFilename;

        public ByteArrayMultipartFile(byte[] data, String filename, String contentType, String originalFilename) {
            this.data = data;
            this.filename = filename;
            this.contentType = contentType;
            this.originalFilename = originalFilename;
        }

        // 파라미터 이름
        @Override
        public String getName() {
            return filename;
        }

        // 실제 파일이름
        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return data.length == 0;
        }

        @Override
        public long getSize() {
            return data.length;
        }

        @Override
        public byte[] getBytes() {
            return data;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(data);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), data);
        }
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

    private String extractBaseName(String originalFilename) {
        if (originalFilename == null) return "sticker";
        int dot = originalFilename.lastIndexOf('.');
        return dot > 0 ? originalFilename.substring(0, dot) : originalFilename;
    }

}
