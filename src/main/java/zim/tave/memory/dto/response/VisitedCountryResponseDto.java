package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import zim.tave.memory.domain.VisitedCountry;

@Getter
@AllArgsConstructor
@Schema(description = "방문한 국가 응답 DTO")
public class VisitedCountryResponseDto {

    @Schema(description = "방문한 국가 ID", example = "1")
    private final Long visitedCountryId;

    @Schema(description = "국가 코드", example = "KR")
    private final String countryCode;

    @Schema(description = "국가명", example = "대한민국")
    private final String countryName;

    @Schema(description = "국가 이모지", example = "🇰🇷")
    private final String emoji;

    @Schema(description = "감정명", example = "행복")
    private final String emotionName;

    @Schema(description = "색상", example = "#FF6B6B")
    private final String color;

    @Schema(description = "사용자 ID", example = "1")
    private final Long userId;

    public static VisitedCountryResponseDto from(VisitedCountry visitedCountry) {
        return new VisitedCountryResponseDto(
                visitedCountry.getVisitedCountryId(),
                visitedCountry.getCountry().getCountryCode(),
                visitedCountry.getCountry().getCountryName(),
                visitedCountry.getCountry().getEmoji(),
                visitedCountry.getEmotion().getName(),
                visitedCountry.getColor(),
                visitedCountry.getUser().getId()
        );
    }
}
