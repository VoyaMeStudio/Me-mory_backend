package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.BoardStickerMap;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "보드에 붙은 스티커 상세 정보")
public class BoardStickerDetailDto {

    @Schema(description = "보드-스티커 매핑 ID", example = "1")
    private Long boardStickerId;

    @Schema(description = "스티커 ID", example = "3")
    private Long stickerId;

    @Schema(description = "스티커 이미지 URL", example = "https://example.com/sticker3.png")
    private String imageUrl;

    @Schema(description = "스티커 X 좌표", example = "120.5")
    private double posX;

    @Schema(description = "스티커 Y 좌표", example = "88.3")
    private double posY;

    @Schema(description = "스티커 회전값", example = "15.0")
    private double rotation;

    public static BoardStickerDetailDto from(BoardStickerMap map) {
        return BoardStickerDetailDto.builder()
                .boardStickerId(map.getBoardStickerId())
                .stickerId(map.getSticker().getStickerId())
                .imageUrl(map.getSticker().getImageUrl())
                .posX(map.getPosX().doubleValue())
                .posY(map.getPosY().doubleValue())
                .rotation(map.getRotation().doubleValue())
                .build();
    }
}
