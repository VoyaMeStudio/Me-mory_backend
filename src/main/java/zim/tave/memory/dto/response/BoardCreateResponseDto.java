package zim.tave.memory.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Board;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "보드 생성 응답 DTO")
public class BoardCreateResponseDto {

    @Schema(description = "생성된 보드 ID", example = "21")
    private Long boardId;

    @Schema(description = "보드 제목", example = "나의 첫 보드")
    private String title;

    @Schema(description = "보드 테마 ID", example = "3")
    private Long boardThemeId;

    @Schema(description = "보드 생성 시각 (UTC 기준, ISO-8601 형식). 프론트엔드에서는 사용자의 로컬 타임존으로 변환하여 표시해야 합니다.", example = "2025-12-01T14:20:00.000Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt;

    @Schema(description = "보드 최종 수정 시각 (UTC 기준, ISO-8601 형식). 프론트엔드에서는 사용자의 로컬 타임존으로 변환하여 표시해야 합니다.", example = "2025-12-01T14:20:00.000Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime updatedAt;

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
