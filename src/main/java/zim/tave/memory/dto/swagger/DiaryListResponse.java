package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.dto.DiaryResponseDto;

import java.util.List;

@Schema(name = "DiaryListResponse")
public class DiaryListResponse {
    public int code;
    public String message;
    public List<DiaryResponseDto> data;
}

