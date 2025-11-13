package zim.tave.memory.global.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;

import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDto<?>> handleCustomException(CustomException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorCode errorCode = ex.getErrorCode();

        // 에러 로그 기록
        log.error("[{}] CustomException occurred: {}", errorId, errorCode.getMessage(), ex);

        // ErrorCode -> ResponseCode 매핑
        ResponseCode responseCode = switch (errorCode) {
            case LOGIN_FAIL -> ResponseCode.LOGIN_FAIL;
            case USER_NOT_FOUND -> ResponseCode.USER_NOT_FOUND;
            case INVALID_TOKEN -> ResponseCode.INVALID_TOKEN;
            case AUTHENTICATION_FAILED -> ResponseCode.AUTHENTICATION_FAILED;
            case ACCESS_DENIED -> ResponseCode.ACCESS_DENIED;
            case INVALID_COUNTRY_CODE -> ResponseCode.INVALID_COUNTRY_CODE;
            case INVALID_COUNTRY_EMOJI -> ResponseCode.INVALID_COUNTRY_EMOJI;
            case COUNTRY_NOT_FOUND -> ResponseCode.COUNTRY_NOT_FOUND;
            case EMOTION_NOT_FOUND -> ResponseCode.EMOTION_NOT_FOUND;
            case DEFAULT_EMOTION_NOT_CONFIGURED -> ResponseCode.DEFAULT_EMOTION_NOT_CONFIGURED;
            case VISITED_COUNTRY_NOT_FOUND -> ResponseCode.VISITED_COUNTRY_NOT_FOUND;
            case VALIDATION_ERROR -> ResponseCode.VALIDATION_ERROR;
            default -> ResponseCode.SERVER_ERROR;
        };

        // 사용자에게는 errorId 포함 메시지로 전달
        String safeMessage = responseCode.getMessage() + " (errorId: " + errorId + ")";

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseDto.error(responseCode, safeMessage));
    }

    // 예상치 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<?>> handleException(Exception ex) {
        String errorId = UUID.randomUUID().toString();

        // 로그에 StackTrace 포함 기록
        log.error("[{}] Unexpected exception: {}", errorId, ex.getMessage(), ex);

        // 사용자에게는 안전한 메시지만 전달
        String safeMessage = "서버 오류가 발생했습니다. (errorId: " + errorId + ")";

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ResponseCode.SERVER_ERROR, safeMessage));
    }
}
