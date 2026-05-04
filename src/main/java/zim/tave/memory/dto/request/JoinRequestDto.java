package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JoinRequestDto {

    @Schema(description = "사용자 성(필수)", example = "KANG")
    private String surName;

    @Schema(description = "사용자 이름(필수)", example = "JIHYE")
    private String firstName;

    @Schema(description = "사용자 한글 이름(필수)", example = "강지혜")
    private String koreanName;

    @Schema(description = "생년월일(필수)[yyyy-MM-dd]", example = "2000-01-01")
    private LocalDate birth;

    @Schema(description = "국가명(미입력 시 'REPUBLIC OF KOREA')", example = "REPUBLIC OF KOREA")
    private String nationality;

    @Schema(description = "프로필 이미지 URL (미입력 시 카카오 프로필 이미지 사용)", example = "https://example.com/image.jpg")
    private String profileImageUrl;

    @Schema(description = "알림 동의 여부 (기본: true, false는 알림 전송X)", example = "true")
    private Boolean alarm;

    // --- null-safe ---
    public String getNationality() {
        return (nationality == null || nationality.isBlank()) ? "REPUBLIC OF KOREA" : nationality;
    }

    public Boolean getAlarm() {
        return (alarm == null) ? true : alarm;
    }

    // 유효한 URL인지 검사 (http/https만 허용)
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?)://[^\\s/$.?#].[^\\s]*$", Pattern.CASE_INSENSITIVE
    );

    // null/blank면 카카오 이미지를 사용하므로 검사하지 않음
    public String getValidatedProfileImageUrl() {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return null;
        }
        if (!URL_PATTERN.matcher(profileImageUrl).matches()) {
            throw new CustomException(ErrorCode.INVALID_PROFILE_IMAGE_URL);
        }
        return profileImageUrl;
    }

}
