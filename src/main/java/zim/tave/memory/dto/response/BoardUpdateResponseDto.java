package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Board;
import zim.tave.memory.domain.BoardStickerMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "보드 수정 응답 DTO")
public class BoardUpdateResponseDto {

    @Schema(description = "보드 ID", example = "21")
    private Long boardId;

    @Schema(description = "보드 최종 수정 시각", example = "2025-12-01T17:30:00")
    private String updatedAt;

    @Schema(description = "적용된 스티커 리스트")
    private List<BoardStickerItem> stickers;

    public static BoardUpdateResponseDto from(Board board, List<BoardStickerMap> stickerMaps) {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        List<BoardStickerItem> stickerItems = stickerMaps.stream()
                .map(BoardStickerItem::from)
                .toList();

        return new BoardUpdateResponseDto(
                board.getBoardId(),
                board.getUpdatedAt().format(formatter),
                stickerItems
        );
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoardStickerItem {


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

        public static BoardStickerItem from(BoardStickerMap map) {
            return new BoardStickerItem(
                    map.getBoardStickerId(),
                    map.getSticker().getStickerId(),
                    map.getPosX().doubleValue(),
                    map.getPosY().doubleValue(),
                    map.getRotation().doubleValue()
            );
        }
    }
}
