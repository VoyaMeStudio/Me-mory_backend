package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.dto.CountrySearchResponseDto;

import java.util.List;

@Schema(name = "CountrySearchResponse")
public class CountrySearchResponse {
    public int code;
    public String message;
    public List<CountrySearchResponseDto> data;
}

