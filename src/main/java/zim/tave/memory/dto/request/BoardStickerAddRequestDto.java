package zim.tave.memory.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardStickerAddRequestDto {

    private Long stickerId;

    private double posX;

    private double posY;

    private double rotation;
}
