package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Schema(description = "과거 여행 생성 요청")
public class CreatePastTripRequest {

    @Schema(description = "여행 이름 (필수)", example = "유럽 배낭 여행", maxLength = 14)
    private String tripName;

    @Schema(description = "여행 설명", example = "파리와 로마를 방문한 여행", maxLength = 56)
    private String description;

    @Schema(description = "여행 시작일 (필수)", example = "2023-05-01")
    private LocalDate startDate;

    @Schema(description = "여행 종료일 (필수)", example = "2023-05-14")
    private LocalDate endDate;

    @Schema(description = "방문한 국가 코드 목록 (필수)", example = "[\"FR\", \"IT\"]")
    private List<String> countryCodes;

    @Schema(description = "감정 ID (선택사항, 미입력 시 기본 감정 사용)", example = "2")
    private Long emotionId;
}

