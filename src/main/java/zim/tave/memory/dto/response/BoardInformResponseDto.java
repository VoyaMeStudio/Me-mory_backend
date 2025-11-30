package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import zim.tave.memory.domain.Board;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "보드 정보 요약 DTO")
public class BoardInformResponseDto {

    @Schema(description = "생성된 보드 ID", example = "21")
    private Long boardId;

    @Schema(description = "보드 제목", example = "나의 첫 보드")
    private String title;

    @Schema(description = "보드 테마 ID", example = "3")
    private Long boardThemeId;

    @Schema(description = "보드 생성 시각", example = "2025-12-01T14:20:00")
    private LocalDateTime createdAt;

    @Schema(description = "보드 최종 수정 시각", example = "2025-12-01T14:20:00")
    private LocalDateTime updatedAt;

    @Schema(description = "보드에 포함된 스티커 개수", example = "3")
    private int stickerCount;

    @Schema(description = "보드 내 스티커 이미지 URL 리스트")
    private List<String> stickerUrls;

    public static BoardInformResponseDto from(Board board, List<String> stickerUrls) {
        return new BoardInformResponseDto(
                board.getBoardId(),
                board.getTitle(),
                board.getBoardTheme().getBoardThemeId(),
                board.getCreatedAt(),
                board.getUpdatedAt(),
                stickerUrls.size(),
                stickerUrls
        );
    }
}
