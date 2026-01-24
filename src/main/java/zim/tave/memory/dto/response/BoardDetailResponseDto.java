package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Board;
import zim.tave.memory.domain.BoardStickerMap;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "단일 보드 상세 조회 응답 DTO")
public class BoardDetailResponseDto {

    @Schema(description = "보드 ID", example = "21")
    private Long boardId;

    @Schema(description = "보드 제목", example = "겨울 여행 기록")
    private String title;

    @Schema(description = "보드 테마 ID", example = "3")
    private Long boardThemeId;

    @Schema(description = "테마 썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "생성 시각 (UTC 기준, ISO-8601 형식). 프론트엔드에서는 사용자의 로컬 타임존으로 변환하여 표시해야 합니다.", example = "2025-12-01T14:20:00.000Z")
    private OffsetDateTime createdAt;

    @Schema(description = "수정 시각 (UTC 기준, ISO-8601 형식). 프론트엔드에서는 사용자의 로컬 타임존으로 변환하여 표시해야 합니다.", example = "2025-12-01T17:30:00.000Z")
    private OffsetDateTime updatedAt;

    @Schema(description = "스티커 목록")
    private List<BoardStickerDetailDto> stickers;

    public static BoardDetailResponseDto from(Board board, List<BoardStickerMap> stickerMaps) {
        return BoardDetailResponseDto.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .boardThemeId(board.getBoardTheme().getBoardThemeId())
                .thumbnailUrl(board.getBoardTheme().getThumbnailUrl())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .stickers(stickerMaps.stream()
                        .map(BoardStickerDetailDto::from)
                        .toList())
                .build();
    }
}
