package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardStickerAddRequestDto {

    @Schema(description = "스티커 ID", example = "3")
    private Long stickerId;

    @Schema(description = "스티커 X 좌표", example = "120.5")
    private double posX;

    @Schema(description = "스티커 Y 좌표", example = "88.3")
    private double posY;

    @Schema(description = "스티커 회전값", example = "15.0")
    private double rotation;
}
