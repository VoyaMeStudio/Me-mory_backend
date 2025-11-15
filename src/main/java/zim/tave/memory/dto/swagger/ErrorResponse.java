package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse")
public class ErrorResponse {
    public int code;
    public String message;
    public Object data;
}

