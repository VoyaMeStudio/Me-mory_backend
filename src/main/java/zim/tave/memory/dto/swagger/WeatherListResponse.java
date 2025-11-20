package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.dto.WeatherResponseDto;

import java.util.List;

@Schema(name = "WeatherListResponse")
public class WeatherListResponse {
    public int code;
    public String message;
    public List<WeatherResponseDto> data;
}

