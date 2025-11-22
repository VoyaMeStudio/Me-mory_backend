package zim.tave.memory.config.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;

import java.util.*;

@Component
public class SwaggerErrorCodeExampleCustomizer implements OperationCustomizer {

    private static final String EXAMPLE_ERROR_ID = "0cde4715-c546-4af5-ae2e-6c2c324cfa00";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ApiErrorCodeExamples annotation = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples.class);

        if (annotation == null) {
            return operation;
        }

        ErrorCode[] errorCodes = annotation.value();
        if (errorCodes.length == 0) {
            return operation;
        }

        // ErrorCode를 HttpStatus별로 그룹화
        Map<org.springframework.http.HttpStatus, List<ErrorCode>> groupedByStatus = new LinkedHashMap<>();
        for (ErrorCode errorCode : errorCodes) {
            org.springframework.http.HttpStatus httpStatus = errorCode.getHttpStatus();
            groupedByStatus.computeIfAbsent(httpStatus, k -> new ArrayList<>()).add(errorCode);
        }

        // 각 HttpStatus에 대해 ApiResponse 생성 또는 업데이트
        for (Map.Entry<org.springframework.http.HttpStatus, List<ErrorCode>> entry : groupedByStatus.entrySet()) {
            org.springframework.http.HttpStatus httpStatus = entry.getKey();
            List<ErrorCode> errorCodesForStatus = entry.getValue();

            String statusCode = String.valueOf(httpStatus.value());
            ApiResponse apiResponse = operation.getResponses().get(statusCode);

            if (apiResponse == null) {
                apiResponse = new ApiResponse();
                operation.getResponses().addApiResponse(statusCode, apiResponse);
            }

            // Content가 없으면 생성
            if (apiResponse.getContent() == null) {
                apiResponse.setContent(new Content());
            }

            // MediaType이 없으면 생성
            MediaType mediaType = apiResponse.getContent().get("application/json");
            if (mediaType == null) {
                mediaType = new MediaType();
                apiResponse.getContent().addMediaType("application/json", mediaType);
            }

            // Schema 설정 (ApiResponseDto 참조)
            if (mediaType.getSchema() == null) {
                io.swagger.v3.oas.models.media.Schema<?> schema = new io.swagger.v3.oas.models.media.Schema<>();
                schema.set$ref("#/components/schemas/ApiResponseDto");
                mediaType.setSchema(schema);
            }

            // Examples 추가
            if (mediaType.getExamples() == null) {
                mediaType.setExamples(new LinkedHashMap<>());
            }

            // 각 ErrorCode에 대해 Example 생성
            for (ErrorCode errorCode : errorCodesForStatus) {
                String exampleName = errorCode.name();
                Example example = new Example();

                // ResponseCode 매핑 (GlobalExceptionHandler와 동일한 로직)
                ResponseCode responseCode = mapErrorCodeToResponseCode(errorCode);
                String message = responseCode.getMessage() + " (errorId: " + EXAMPLE_ERROR_ID + ")";

                // JSON 생성
                Map<String, Object> errorResponse = new LinkedHashMap<>();
                errorResponse.put("code", responseCode.getCode());
                errorResponse.put("message", message);
                errorResponse.put("data", null);

                try {
                    String jsonValue = objectMapper.writeValueAsString(errorResponse);
                    example.setValue(jsonValue);
                } catch (Exception e) {
                    // JSON 변환 실패 시 직접 문자열 생성
                    example.setValue(String.format(
                        "{\"code\":%d,\"message\":\"%s\",\"data\":null}",
                        responseCode.getCode(),
                        message.replace("\"", "\\\"")
                    ));
                }

                mediaType.getExamples().put(exampleName, example);
            }
        }

        return operation;
    }

    private ResponseCode mapErrorCodeToResponseCode(ErrorCode errorCode) {
        return switch (errorCode) {
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
            case KAKAO_LOGIN_REQUIRED -> ResponseCode.KAKAO_LOGIN_REQUIRED;
            case INCOMPLETE_USER_INFO -> ResponseCode.INCOMPLETE_USER_INFO;
            case ALREADY_JOINED -> ResponseCode.ALREADY_JOINED;
            case UNAUTHORIZED_USER -> ResponseCode.UNAUTHORIZED_USER;
            case MISSING_REQUIRED_FIELDS -> ResponseCode.MISSING_REQUIRED_FIELDS;
            case FILE_EMPTY -> ResponseCode.FILE_EMPTY;
            case FILE_TOO_LARGE -> ResponseCode.FILE_TOO_LARGE;
            case FILE_TYPE_NOT_ALLOWED -> ResponseCode.FILE_TYPE_NOT_ALLOWED;
            case FILE_UPLOAD_FAILED -> ResponseCode.FILE_UPLOAD_FAILED;
            default -> ResponseCode.SERVER_ERROR;
        };
    }
}

