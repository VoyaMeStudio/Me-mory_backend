package zim.tave.memory.global.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDto<?>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ResponseCode responseCode = switch (errorCode) {
            case LOGIN_FAIL -> ResponseCode.LOGIN_FAIL;
            case USER_NOT_FOUND -> ResponseCode.USER_NOT_FOUND;
            default -> ResponseCode.SERVER_ERROR;
        };

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseDto.error(responseCode));
    }

    // 예상치 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<?>> handleException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity
                .status(500)
                .body(ApiResponseDto.error(ResponseCode.SERVER_ERROR));
    }
}
