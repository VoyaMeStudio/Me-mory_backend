package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "여행 생성 요청")
public class CreateTripRequest {

    @Schema(description = "여행 이름", example = "제주도 여행", maxLength = 14, required = true)
    private String tripName;

    @Schema(description = "여행 설명", example = "가족과 함께하는 제주도 여행", maxLength = 56)
    private String description;

    @Schema(description = "여행 테마 ID (선택사항, 미입력 시 기본 테마 자동 설정)", example = "1")
    private Long themeId;

    @Schema(description = "여행 시작일", example = "2025-01-01", required = true)
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2025-01-07", required = true)
    private LocalDate endDate;

}
