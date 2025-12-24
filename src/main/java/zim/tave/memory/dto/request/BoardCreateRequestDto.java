package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateRequestDto {

    @Schema(description = "보드 제목", example = "나의 첫 보드")
    private String title;

    @Schema(description = "선택한 보드 테마 ID", example = "3")
    private Long boardThemeId;
}
