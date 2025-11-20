package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.dto.WeatherResponseDto;

@Schema(name = "WeatherSingleResponse")
public class WeatherSingleResponse {
    public int code;
    public String message;
    public WeatherResponseDto data;
}

