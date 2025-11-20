package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import zim.tave.memory.domain.User;

//로그인 후 응답
@Getter
@AllArgsConstructor
public class LoginResponseDto {

    @Schema(description = "발급된 userId", example = "1")
    private Long userId;

    @Schema(description = "기존 회원 여부 확인(true = 이미 가입된 회원 / false = 신규 회원)",
            example = "true")
    private boolean registered; // 기존 회원 여부 확인


    @Schema(description = "카카오 사용자 ID", example = "4317757086")
    private String kakaoId;

    @Schema(description = "프로필 이미지 URL", example = "http://img1.kakaocdn.net/profile.jpeg")
    private String profileImageUrl;

    @Schema(description = "로그인 시 AT 발급", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String jwtAccessToken;

    public static LoginResponseDto from(User user, String jwt, boolean registered) {
        return new LoginResponseDto(
                user.getId(),
                registered,
                user.getKakaoId(),
                user.getProfileImageUrl(),
                jwt
        );
    }
}
