package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Board;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardCreateResponseDto {

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

    public static BoardCreateResponseDto from(Board board) {
        return new BoardCreateResponseDto(
                board.getBoardId(),
                board.getTitle(),
                board.getBoardTheme().getBoardThemeId(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
