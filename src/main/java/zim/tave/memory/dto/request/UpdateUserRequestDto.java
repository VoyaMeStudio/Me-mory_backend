package zim.tave.memory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
