package zim.tave.memory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import zim.tave.memory.dto.response.StickerCreateResponseDto;
import zim.tave.memory.dto.response.StickerListResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.global.common.exception.GlobalExceptionHandler;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.StickerService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class StickerControllerTest {

    @Mock
    private StickerService stickerService;

    @InjectMocks
    private StickerController stickerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stickerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reset(stickerService);
    }

    private void setSecurityContext(Long userId) {
        CustomUserDetails principal = new CustomUserDetails(userId, "test@test.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    @Test
    void 스티커_생성_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "dummy".getBytes()
        );

        StickerCreateResponseDto responseDto =
                StickerCreateResponseDto.builder()
                        .imageUrl("url")
                        .name("test")
                        .build();

        given(stickerService.createSticker(eq(userId), any()))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(multipart("/api/users/me/stickers")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code")
                        .value(ResponseCode.STICKER_CREATE_SUCCESS.getCode()));

        verify(stickerService).createSticker(eq(userId), any());
    }

    @Test
    void 스티커_생성_실패_파일문제() throws Exception {
        Long userId = 1L;
        setSecurityContext(userId);

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                new byte[]{}
        );

        given(stickerService.createSticker(eq(userId), any()))
                .willThrow(new CustomException(ErrorCode.FILE_EMPTY));

        mockMvc.perform(multipart("/api/users/me/stickers")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 내_스티커_목록_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        StickerListResponseDto responseDto = StickerListResponseDto.builder()
                .stickers(Collections.emptyList())
                .build();

        given(stickerService.getMyStickers(userId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/users/me/stickers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(stickerService).getMyStickers(userId);
    }
}
