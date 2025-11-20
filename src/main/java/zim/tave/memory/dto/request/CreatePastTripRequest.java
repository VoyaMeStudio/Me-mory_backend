package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Schema(description = "과거 여행 생성 요청")
public class CreatePastTripRequest {

    @NotBlank
    @Schema(description = "여행 이름", example = "유럽 배낭 여행", maxLength = 14, required = true)
    private String tripName;

    @Schema(description = "여행 설명", example = "파리와 로마를 방문한 여행", maxLength = 56)
    private String description;

    @NotNull
    @Schema(description = "여행 시작일", example = "2023-05-01", required = true)
    private LocalDate startDate;

    @NotNull
    @Schema(description = "여행 종료일", example = "2023-05-14", required = true)
    private LocalDate endDate;

    @NotEmpty
    @Schema(description = "방문한 국가 코드 목록", example = "[\"FRA\", \"ITA\"]", required = true)
    private List<String> countryCodes;

    @NotNull
    @Schema(description = "감정 ID", example = "2", required = true)
    private Long emotionId;
}

