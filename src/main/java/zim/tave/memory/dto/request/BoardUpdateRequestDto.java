package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "보드 수정 요청 DTO (스티커 전체 리스트 저장)")
public class BoardUpdateRequestDto {

    @Schema(description = "변경할 스티커 리스트")
    private List<StickerUpdateItem> stickers;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "스티커 수정 단일 정보")
    public static class StickerUpdateItem {

        @Schema(description = "스티커 ID", example = "3")
        private Long stickerId;

        @Schema(description = "X 좌표", example = "120.5")
        private double posX;

        @Schema(description = "Y 좌표", example = "88.3")
        private double posY;

        @Schema(description = "회전값", example = "15.0")
        private double rotation;
    }
}
