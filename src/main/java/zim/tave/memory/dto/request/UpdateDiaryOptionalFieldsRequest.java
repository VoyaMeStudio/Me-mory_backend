package zim.tave.memory.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDiaryOptionalFieldsRequest {

    private String city;
    private String detailedLocation;
    private String content;
    private Long emotionId;
    private Long weatherId;
}
