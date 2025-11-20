package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.dto.TripThemeResponseDto;

import java.util.List;

@Schema(name = "TripThemeListResponse")
public class TripThemeListResponse {
    public int code;
    public String message;
    public List<TripThemeResponseDto> data;
}

