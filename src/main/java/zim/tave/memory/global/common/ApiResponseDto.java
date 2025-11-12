package zim.tave.memory.global.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDto<T> {

    private int code;
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
