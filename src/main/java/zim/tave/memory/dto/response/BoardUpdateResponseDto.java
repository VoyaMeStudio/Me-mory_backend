package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Board;
import zim.tave.memory.domain.BoardStickerMap;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "보드 수정 응답 DTO")
public class BoardUpdateResponseDto {

    @Schema(description = "보드 ID", example = "21")
    private Long boardId;

    @Schema(description = "보드 최종 수정 시각", example = "2025-12-01T17:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "적용된 스티커 리스트")
    private List<BoardStickerItem> stickers;

    public static BoardUpdateResponseDto from(Board board, List<BoardStickerMap> stickerMaps) {

        List<BoardStickerItem> stickerItems = stickerMaps.stream()
                .map(BoardStickerItem::from)
                .toList();

        return new BoardUpdateResponseDto(
                board.getBoardId(),
                board.getUpdatedAt(),
                stickerItems
        );
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoardStickerItem {

        private Long boardStickerId;
        private Long stickerId;
        private double posX;
        private double posY;
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
