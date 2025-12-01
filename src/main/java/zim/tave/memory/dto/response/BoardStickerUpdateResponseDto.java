package zim.tave.memory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import zim.tave.memory.domain.BoardStickerMap;

@Getter
@AllArgsConstructor
public class BoardStickerUpdateResponseDto {

    private Long boardStickerId;
    private Long stickerId;
    private double posX;
    private double posY;
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
