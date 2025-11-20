package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "StringResponse")
public class StringResponse {
    public int code;
    public String message;
    public String data;
}

