package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", example = "{\"code\": 401, \"message\": \"인증에 실패했습니다. (errorId: 0cde4715-c546-4af5-ae2e-6c2c324cfa48)\", \"data\": null}")
public class ErrorResponse {
    @Schema(description = "에러 코드", example = "401")
    public int code;
    
    @Schema(description = "에러 메시지", example = "인증에 실패했습니다. (errorId: 0cde4715-c546-4af5-ae2e-6c2c324cfa48)")
    public String message;
    
    @Schema(description = "에러 데이터 (일반적으로 null)", example = "null", nullable = true)
    public Object data;
}

