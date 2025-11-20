package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import zim.tave.memory.domain.Country;

@Getter
@AllArgsConstructor
@Schema(description = "국가 검색 응답 DTO")
public class CountrySearchResponseDto {

    @Schema(description = "국가 코드", example = "KR")
    private final String countryCode;

    @Schema(description = "국가명", example = "대한민국")
    private final String countryName;

    @Schema(description = "국가 이모지", example = "🇰🇷")
    private final String emoji;

    public static CountrySearchResponseDto from(Country country) {
        return new CountrySearchResponseDto(
                country.getCountryCode(),
                country.getCountryName(),
                country.getEmoji()
        );
    }
}
