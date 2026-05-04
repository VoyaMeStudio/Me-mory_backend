package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.response.UserResponseDto;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "회원 정보 수정 요청 DTO")
public class UpdateUserRequestDto {

    @Schema(description = "성", example = "CHOE")
    private String surName;

    @Schema(description = "이름", example = "HYUNGWON")
    private String firstName;

    @Schema(description = "한국 이름", example = "최형원")
    private String koreanName;

    @Schema(description = "생년월일 [yyyy-MM-dd]", example = "2004-10-10")
    private LocalDate birth;

    @Schema(description = "국적", example = "REPUBLIC OF KOREA")
    private String nationality;

    @Schema(description = "알림수신동의", example = "true")
    private Boolean alarm;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/newimage.jpg")
    private String profileImageUrl;

    public static UserResponseDto from(User user) {

        Boolean alarm = null;
        if (user.getSetting() != null) {
            alarm = user.getSetting().getAlarm();
        }

        return new UserResponseDto(
                user.getId(),
                user.getKakaoId(),
                user.getProfileImageUrl(),
                user.getSurName(),
                user.getFirstName(),
                user.getKoreanName(),
                user.getBirth(),
                user.getNationality(),
                user.getDiaryCount(),
                user.getVisitedCountryCount(),
                user.getFlags(),
                alarm
        );
    }
}
