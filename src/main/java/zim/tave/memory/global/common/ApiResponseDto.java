package zim.tave.memory.global.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ApiResponseDto", description = "공통 응답 형식")
public class ApiResponseDto<T> {

    @Schema(description = "응답 코드")
    private int code;

    @Schema(description = "응답 메시지")
    private String message;
    private T data;

    // 성공 응답
    public static <T> ApiResponseDto<T> success(ResponseCode code, T data) {
        return ApiResponseDto.<T>builder()
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .build();
    }

    // 실패 응답
    public static <T> ApiResponseDto<T> error(ResponseCode code, String customMessage) {
        return ApiResponseDto.<T>builder()
                .code(code.getCode())
                .message(customMessage != null ? customMessage : code.getMessage())
                .data(null)
                .build();
    }
}
