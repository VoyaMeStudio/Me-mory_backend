package zim.tave.memory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import zim.tave.memory.config.FileUploadProperties;
import zim.tave.memory.domain.Sticker;
import zim.tave.memory.domain.User;
import zim.tave.memory.domain.UserSticker;
import zim.tave.memory.dto.response.StickerCreateResponseDto;
import zim.tave.memory.dto.response.StickerListResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.repository.StickerRepository;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.repository.UserStickerRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StickerServiceTest {

    @Mock
    private UserStickerRepository userStickerRepository;
    @Mock
    StickerRepository stickerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private FileUploadProperties fileUploadProperties;
    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private StickerService stickerService;

    @Test
    void 스티커_생성_성공() throws Exception {
        // given
        Long userId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "dummy".getBytes()
        );

        User user = new User();
        user.setId(userId);

        when(fileUploadProperties.getMaxFileSize()).thenReturn(10_000L);
        when(fileUploadProperties.getAllowedImageTypes()).thenReturn(List.of("image/jpeg"));
        when(fileUploadProperties.getAllowedImageExtensions()).thenReturn(List.of(".jpg"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(restTemplate.exchange(anyString(), any(), any(), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok("converted".getBytes()));

        when(s3Uploader.upload(any(), any())).thenReturn("s3-url");

        when(stickerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        StickerCreateResponseDto result = stickerService.createSticker(userId, file);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void 내_스티커_목록_조회_성공() {
        // given
        Long userId = 1L;

        Sticker sticker = Sticker.builder()
                .name("test")
                .imageUrl("url")
                .build();

        UserSticker userSticker = UserSticker.builder()
                .sticker(sticker)
                .build();

        when(userStickerRepository.findByUserId(userId))
                .thenReturn(List.of(userSticker));

        // when
        StickerListResponseDto result = stickerService.getMyStickers(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStickers()).hasSize(1);
    }

    @Test
    void 파일이_null이면_예외() {
        assertThatThrownBy(() -> stickerService.createSticker(1L, null))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 파일_empty면_예외() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[]{}
        );

        assertThatThrownBy(() -> stickerService.createSticker(1L, file))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저없으면_예외() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "data".getBytes()
        );

        when(fileUploadProperties.getMaxFileSize()).thenReturn(10_000L);
        when(fileUploadProperties.getAllowedImageTypes()).thenReturn(List.of("image/jpeg"));
        when(fileUploadProperties.getAllowedImageExtensions()).thenReturn(List.of(".jpg"));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stickerService.createSticker(1L, file))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void S3_업로드_실패() throws Exception {
        Long userId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "data".getBytes()
        );

        User user = new User();
        user.setId(userId);

        when(fileUploadProperties.getMaxFileSize()).thenReturn(10_000L);
        when(fileUploadProperties.getAllowedImageTypes()).thenReturn(List.of("image/jpeg"));
        when(fileUploadProperties.getAllowedImageExtensions()).thenReturn(List.of(".jpg"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(restTemplate.exchange(anyString(), any(), any(), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok("converted".getBytes()));

        when(s3Uploader.upload(any(), any())).thenThrow(new IOException());

        assertThatThrownBy(() -> stickerService.createSticker(userId, file))
                .isInstanceOf(CustomException.class);
    }
}
