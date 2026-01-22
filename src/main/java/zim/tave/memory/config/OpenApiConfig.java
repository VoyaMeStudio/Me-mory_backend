package zim.tave.memory.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
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

    @Bean
    public OpenApiCustomizer apiResponseDtoVoidSchemaCustomizer() {
        return openApi -> {
            // ApiResponseDto<Void> 스키마를 명시적으로 등록
            Schema<?> voidSchema = new Schema<>();
            voidSchema.setType("object");
            voidSchema.setDescription("공통 응답 형식 (에러 응답용 - data 필드는 null)");
            
            Schema<?> codeSchema = new Schema<>();
            codeSchema.setType("integer");
            codeSchema.setDescription("응답 코드");
            voidSchema.addProperty("code", codeSchema);
            
            Schema<?> messageSchema = new Schema<>();
            messageSchema.setType("string");
            messageSchema.setDescription("응답 메시지");
            voidSchema.addProperty("message", messageSchema);
            
            Schema<?> dataSchema = new Schema<>();
            dataSchema.setType("object");
            dataSchema.setNullable(true);
            dataSchema.setDescription("데이터 (에러 응답에서는 항상 null)");
            voidSchema.addProperty("data", dataSchema);
            
            // 스키마 등록
            openApi.getComponents().addSchemas("ApiResponseDtoVoid", voidSchema);
        };
    }
}