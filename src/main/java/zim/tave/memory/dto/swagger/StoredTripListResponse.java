package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.dto.StoredTripListResponseDto;

@Schema(name = "StoredTripListResponse")
public class StoredTripListResponse {
    public int code;
    public String message;
    public StoredTripListResponseDto data;
}

