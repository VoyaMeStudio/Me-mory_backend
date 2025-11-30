package zim.tave.memory.dto.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_WITH_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateTimeString = p.getText().trim();
        
        try {
            // Z가 포함된 경우 (ISO 8601 with timezone, 예: 2025-11-30T11:20:01.570Z)
            if (dateTimeString.endsWith("Z")) {
                // Z를 제거하고 Instant로 파싱
                String withoutZ = dateTimeString.substring(0, dateTimeString.length() - 1);
                if (withoutZ.contains(".")) {
                    // 밀리초 포함: 2025-11-30T11:20:01.570
                    Instant instant = Instant.parse(dateTimeString);
                    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                } else {
                    // 밀리초 없음: 2025-11-30T11:20:01
                    Instant instant = Instant.parse(dateTimeString);
                    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                }
            }
            
            // 타임존 오프셋이 포함된 경우 (예: +09:00, -05:00)
            if (dateTimeString.contains("+") || (dateTimeString.contains("-") && dateTimeString.length() > 19 && dateTimeString.lastIndexOf("-") > 10)) {
                Instant instant = Instant.parse(dateTimeString);
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            }
            
            // 밀리초가 포함된 경우 (Z 없음)
            if (dateTimeString.contains(".")) {
                // .SSS 형식 처리 (최대 23자: yyyy-MM-ddTHH:mm:ss.SSS)
                if (dateTimeString.length() > 23) {
                    dateTimeString = dateTimeString.substring(0, 23);
                }
                return LocalDateTime.parse(dateTimeString, FORMATTER_WITH_MILLIS);
            }
            
            // 기본 형식 (yyyy-MM-ddTHH:mm:ss)
            return LocalDateTime.parse(dateTimeString, FORMATTER);
            
        } catch (DateTimeParseException e) {
            // CustomException으로 변환하여 던지기 (Jackson이 HttpMessageNotReadableException으로 감싸서 던짐)
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        } catch (Exception e) {
            // Instant.parse나 다른 파싱에서 발생하는 예외 처리
            Throwable cause = e.getCause();
            if (e instanceof DateTimeParseException || cause instanceof DateTimeParseException) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR);
            }
            // 예상치 못한 예외는 IOException으로 던지기
            throw new IOException("날짜 및 시간 형식이 올바르지 않습니다. ISO 8601 형식(yyyy-MM-ddTHH:mm:ss 또는 yyyy-MM-ddTHH:mm:ss.SSSZ)을 사용해주세요. 입력값: " + dateTimeString, e);
        }
    }
}

