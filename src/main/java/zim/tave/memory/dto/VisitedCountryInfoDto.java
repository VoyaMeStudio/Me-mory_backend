package zim.tave.memory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "타임라인 방문 국가 정보 DTO")
public class VisitedCountryInfoDto {

    @Schema(description = "국가 코드", example = "KR")
    private final String countryCode;

    @Schema(description = "국가명", example = "대한민국")
    private final String countryName;

    @Schema(description = "국가 이모지", example = "🇰🇷")
    private final String emoji;

    @Schema(description = "감정명", example = "설렘")
    private final String emotionName;

    @Schema(description = "감정 색상 코드", example = "#FF6B6B")
    private final String emotionColor;
}

