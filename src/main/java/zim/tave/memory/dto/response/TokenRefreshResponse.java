package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenRefreshResponse {

    @Schema(description = "새로 발급된 Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String accessToken;
}
