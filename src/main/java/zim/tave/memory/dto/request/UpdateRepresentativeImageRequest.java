package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대표 이미지 변경 요청")
public class UpdateRepresentativeImageRequest {

    @Schema(description = "대표사진으로 설정할 이미지 ID (필수)", example = "1")
    private Long imageId;
}
