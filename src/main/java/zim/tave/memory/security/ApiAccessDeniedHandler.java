package zim.tave.memory.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        
        String errorId = UUID.randomUUID().toString();
        String logMessage = String.format("접근 권한 없음: %s", accessDeniedException.getMessage());
        
        // 로그 기록
        log.error("[{}] Access denied: {}", errorId, logMessage, accessDeniedException);
        
        // 사용자에게는 errorId 포함 메시지로 전달
        String safeMessage = ResponseCode.ACCESS_DENIED.getMessage() + " (errorId: " + errorId + ")";
        
        ApiResponseDto<?> body = ApiResponseDto.error(ResponseCode.ACCESS_DENIED, safeMessage);
        
        writeResponse(response, HttpStatus.FORBIDDEN, body);
    }

    private void writeResponse(HttpServletResponse response,
                               HttpStatus status,
                               ApiResponseDto<?> body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}

