package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.dto.DiaryResponseDto;

@Schema(name = "DiaryResponse")
public class DiaryResponse {
    public int code;
    public String message;
    public DiaryResponseDto data;
}

