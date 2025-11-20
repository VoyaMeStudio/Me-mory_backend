package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "VoidResponse", example = "{\"code\": 200, \"message\": \"성공하였습니다.\", \"data\": null}")
public class VoidResponse {
    @Schema(description = "응답 코드", example = "200")
    public int code;
    
    @Schema(description = "응답 메시지", example = "성공하였습니다.")
    public String message;
    
    @Schema(description = "응답 데이터 (일반적으로 null)", example = "null", nullable = true)
    public Object data;
}

