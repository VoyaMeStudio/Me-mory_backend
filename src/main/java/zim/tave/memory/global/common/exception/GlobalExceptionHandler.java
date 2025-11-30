package zim.tave.memory.global.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;

import java.time.format.DateTimeParseException;
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
            case INVALID_REQUEST -> ResponseCode.INVALID_REQUEST;
            case LOGIN_FAIL -> ResponseCode.LOGIN_FAIL;
            case USER_NOT_FOUND -> ResponseCode.USER_NOT_FOUND;
            case INVALID_TOKEN -> ResponseCode.INVALID_TOKEN;
            case AUTHENTICATION_FAILED -> ResponseCode.AUTHENTICATION_FAILED;
            case ACCESS_DENIED -> ResponseCode.ACCESS_DENIED;
            case TRIP_UPDATE_FORBIDDEN -> ResponseCode.ACCESS_DENIED;
            case INVALID_COUNTRY_CODE -> ResponseCode.INVALID_COUNTRY_CODE;
            case INVALID_COUNTRY_EMOJI -> ResponseCode.INVALID_COUNTRY_EMOJI;
            case COUNTRY_NOT_FOUND -> ResponseCode.COUNTRY_NOT_FOUND;
            case TRIP_NOT_FOUND -> ResponseCode.TRIP_NOT_FOUND;
            case EMOTION_NOT_FOUND -> ResponseCode.EMOTION_NOT_FOUND;
            case DEFAULT_EMOTION_NOT_CONFIGURED -> ResponseCode.DEFAULT_EMOTION_NOT_CONFIGURED;
            case VISITED_COUNTRY_NOT_FOUND -> ResponseCode.VISITED_COUNTRY_NOT_FOUND;
            case VALIDATION_ERROR -> ResponseCode.VALIDATION_ERROR;
            case ALREADY_LOGGED_OUT -> ResponseCode.ALREADY_LOGGED_OUT;
            case DIARY_NOT_FOUND -> ResponseCode.DIARY_NOT_FOUND;
            case IMAGE_NOT_FOUND -> ResponseCode.IMAGE_NOT_FOUND;
            case IMAGE_COUNT_INVALID -> ResponseCode.IMAGE_COUNT_INVALID;
            case CAMERA_TYPES_REQUIRED -> ResponseCode.CAMERA_TYPES_REQUIRED;
            case REPRESENTATIVE_IMAGE_REQUIRED -> ResponseCode.REPRESENTATIVE_IMAGE_REQUIRED;
            case IMAGE_ID_REQUIRED -> ResponseCode.IMAGE_ID_REQUIRED;
            case IMAGE_TRIP_MISMATCH -> ResponseCode.IMAGE_TRIP_MISMATCH;
            case IMAGE_NOT_REPRESENTATIVE -> ResponseCode.IMAGE_NOT_REPRESENTATIVE;
            case TRIP_THEME_NOT_FOUND -> ResponseCode.TRIP_THEME_NOT_FOUND;
            case WEATHER_NOT_FOUND -> ResponseCode.WEATHER_NOT_FOUND;
            case TRIP_NAME_REQUIRED -> ResponseCode.TRIP_NAME_REQUIRED;
            case TRIP_NAME_TOO_LONG -> ResponseCode.TRIP_NAME_TOO_LONG;
            case TRIP_DESCRIPTION_TOO_LONG -> ResponseCode.TRIP_DESCRIPTION_TOO_LONG;
            case NOT_PAST_TRIP -> ResponseCode.NOT_PAST_TRIP;
            case CANNOT_ADD_DIARY_TO_PAST_TRIP -> ResponseCode.CANNOT_ADD_DIARY_TO_PAST_TRIP;
            case KAKAO_LOGIN_REQUIRED -> ResponseCode.KAKAO_LOGIN_REQUIRED;
            case INCOMPLETE_USER_INFO -> ResponseCode.INCOMPLETE_USER_INFO;
            case ALREADY_JOINED -> ResponseCode.ALREADY_JOINED;
            case UNAUTHORIZED_USER -> ResponseCode.UNAUTHORIZED_USER;
            case MISSING_REQUIRED_FIELDS -> ResponseCode.MISSING_REQUIRED_FIELDS;
            case FILE_EMPTY -> ResponseCode.FILE_EMPTY;
            case FILE_TOO_LARGE -> ResponseCode.FILE_TOO_LARGE;
            case FILE_TYPE_NOT_ALLOWED -> ResponseCode.FILE_TYPE_NOT_ALLOWED;
            case FILE_UPLOAD_FAILED -> ResponseCode.FILE_UPLOAD_FAILED;

            //보관
            case TRIP_NOT_STORED -> ResponseCode.TRIP_NOT_STORED;
            case DIARY_NOT_STORED -> ResponseCode.DIARY_NOT_STORED;
            //카카오 로그인
            case KAKAO_TOKEN_MISSING -> ResponseCode.KAKAO_TOKEN_MISSING;
            case KAKAO_INVALID_TOKEN -> ResponseCode.KAKAO_INVALID_TOKEN;
            case KAKAO_UNAUTHORIZED -> ResponseCode.KAKAO_UNAUTHORIZED;
            case KAKAO_SERVER_ERROR -> ResponseCode.KAKAO_SERVER_ERROR;
            case KAKAO_RESPONSE_PARSING_ERROR -> ResponseCode.KAKAO_RESPONSE_PARSING_ERROR;
            case KAKAO_API_UNKNOWN_ERROR -> ResponseCode.KAKAO_API_UNKNOWN_ERROR;

            default -> ResponseCode.SERVER_ERROR;
        };

        // 사용자에게는 errorId 포함 메시지로 전달
        String safeMessage = responseCode.getMessage() + " (errorId: " + errorId + ")";

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseDto.error(responseCode, safeMessage));
    }

    // HttpMessageNotReadableException 처리 (Jackson 파싱 오류)
    // LocalDateTimeDeserializer에서 CustomException을 던지면 Jackson이 이를 HttpMessageNotReadableException으로 감싸서 던짐
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String errorId = UUID.randomUUID().toString();
        
        // 에러 로그 기록
        log.error("[{}] HttpMessageNotReadableException: {}", errorId, ex.getMessage(), ex);
        
        // cause가 CustomException인 경우 (LocalDateTimeDeserializer에서 던진 경우)
        Throwable cause = ex.getCause();
        if (cause instanceof CustomException) {
            // CustomException 핸들러로 위임
            return handleCustomException((CustomException) cause);
        }
        
        // 다른 파싱 오류인 경우
        String message = "요청 본문을 파싱할 수 없습니다. 요청 형식을 확인해주세요.";
        if (cause instanceof DateTimeParseException) {
            DateTimeParseException dtpe = (DateTimeParseException) cause;
            message = String.format("날짜 및 시간 형식이 올바르지 않습니다. 입력값: '%s'. ISO 8601 형식(yyyy-MM-ddTHH:mm:ss 또는 yyyy-MM-ddTHH:mm:ss.SSSZ)을 사용해주세요.", 
                    dtpe.getParsedString());
        }
        
        String safeMessage = message + " (errorId: " + errorId + ")";
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(ResponseCode.VALIDATION_ERROR, safeMessage));
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
