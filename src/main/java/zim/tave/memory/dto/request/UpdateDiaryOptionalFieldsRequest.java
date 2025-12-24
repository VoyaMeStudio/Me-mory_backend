package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "일기 선택 필드 수정 요청")
public class UpdateDiaryOptionalFieldsRequest {

    @Schema(description = "도시명", example = "서귀포시")
    private String city;

    @Schema(description = "상세 위치", example = "성산일출봉")
    private String detailedLocation;

    @Schema(description = "일기 내용", example = "수정된 일기 내용입니다.")
    private String content;

    @Schema(description = "감정 ID", example = "1")
    private Long emotionId;

    @Schema(description = "날씨 ID", example = "1")
    private Long weatherId;
}
