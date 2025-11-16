package zim.tave.memory.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDiaryOptionalFieldsRequest {
    
    private String detailedLocation;
    private Long emotionId;
    private Long weatherId;
} 