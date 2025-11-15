package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.dto.VisitedCountryResponseDto;

@Schema(name = "VisitedCountrySingleResponse")
public class VisitedCountrySingleResponse {
    public int code;
    public String message;
    public VisitedCountryResponseDto data;
}

