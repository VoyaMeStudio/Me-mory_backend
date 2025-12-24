package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "보드 목록 응답 DTO")
public class BoardListResponseDto {

    @Schema(description = "보드 목록")
    private List<BoardInformResponseDto> boards;

    public static BoardListResponseDto from(List<BoardInformResponseDto> boards) {
        return new BoardListResponseDto(boards);
    }
}
