package zim.tave.memory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.Sticker;
import zim.tave.memory.domain.UserSticker;
import zim.tave.memory.dto.response.StickerListResponseDto;
import zim.tave.memory.repository.UserStickerRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StickerServiceTest {

    @Mock
    private UserStickerRepository userStickerRepository;

    @InjectMocks
    private StickerService stickerService;

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
}
