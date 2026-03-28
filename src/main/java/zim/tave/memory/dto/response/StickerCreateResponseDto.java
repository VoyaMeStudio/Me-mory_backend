package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Sticker;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "AI 스티커 생성 응답 DTO")
public class StickerCreateResponseDto {

    @Schema(description = "스티커 ID", example = "42")
    private Long stickerId;

    @Schema(description = "스티커 이름", example = "my_photo")
    private String name;

    @Schema(description = "S3 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/stickers/user-1/abc.png")
    private String imageUrl;

    public static StickerCreateResponseDto from(Sticker sticker) {
        return new StickerCreateResponseDto(
                sticker.getStickerId(),
                sticker.getName(),
                sticker.getImageUrl()
        );
    }
}
