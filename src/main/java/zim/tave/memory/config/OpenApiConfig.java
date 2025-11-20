package zim.tave.memory.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Me-mory API",
        description = "Me-mory 여행 일기 앱 백엔드 API 문서",
        version = "v1"
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "로컬 개발 서버"),
        @Server(url = "/", description = "현재 서버")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT 토큰 인증 (Bearer Token)",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}