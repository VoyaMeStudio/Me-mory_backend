package zim.tave.memory.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        String errorId = UUID.randomUUID().toString();
        String logMessage = String.format("인증 실패: %s", authException.getMessage());
        
        // 로그 기록
        log.error("[{}] Authentication failed: {}", errorId, logMessage, authException);
        
        // 사용자에게는 errorId 포함 메시지로 전달
        String safeMessage = ResponseCode.AUTHENTICATION_FAILED.getMessage() + " (errorId: " + errorId + ")";
        
        ApiResponseDto<?> body = ApiResponseDto.error(ResponseCode.AUTHENTICATION_FAILED, safeMessage);
        
        writeResponse(response, HttpStatus.UNAUTHORIZED, body);
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

