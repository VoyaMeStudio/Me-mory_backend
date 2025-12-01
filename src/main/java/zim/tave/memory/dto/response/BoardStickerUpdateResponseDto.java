package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import zim.tave.memory.domain.BoardStickerMap;

@Getter
@AllArgsConstructor
public class BoardStickerUpdateResponseDto {

    @Schema(description = "보드-스티커 매핑 ID", example = "1")
    private Long boardStickerId;

    @Schema(description = "스티커 ID", example = "3")
    private Long stickerId;

    @Schema(description = "스티커 X 좌표", example = "120.5")
    private double posX;

    @Schema(description = "스티커 Y 좌표", example = "88.3")
    private double posY;

    @Schema(description = "스티커 회전값", example = "15.0")
    private double rotation;

    public static BoardStickerUpdateResponseDto from(BoardStickerMap map) {
        return new BoardStickerUpdateResponseDto(
                map.getBoardStickerId(),
                map.getSticker().getStickerId(),
                map.getPosX().doubleValue(),
                map.getPosY().doubleValue(),
                map.getRotation().doubleValue()
        );
    }
}
