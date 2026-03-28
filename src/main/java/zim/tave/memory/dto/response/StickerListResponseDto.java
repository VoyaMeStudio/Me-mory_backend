package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import zim.tave.memory.domain.Sticker;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "스티커 목록 응답 DTO")
public class StickerListResponseDto {

    @Schema(description = "스티커 목록")
    private List<StickerItemDto> stickers;

    public static StickerListResponseDto from(List<Sticker> stickers) {
        List<StickerItemDto> items = stickers.stream()
                .map(StickerItemDto::from)
                .toList();
        return new StickerListResponseDto(items);
    }

    @Getter
    @AllArgsConstructor
    public static class StickerItemDto {
        private Long stickerId;
        private String name;
        private String imageUrl;

        public static StickerItemDto from(Sticker s) {
            return new StickerItemDto(s.getStickerId(), s.getName(), s.getImageUrl());
        }
    }
}
