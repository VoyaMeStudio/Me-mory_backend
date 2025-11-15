package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.dto.VisitedCountryResponseDto;

import java.util.List;

@Schema(name = "VisitedCountryListResponse")
public class VisitedCountryListResponse {
    public int code;
    public String message;
    public List<VisitedCountryResponseDto> data;
}

