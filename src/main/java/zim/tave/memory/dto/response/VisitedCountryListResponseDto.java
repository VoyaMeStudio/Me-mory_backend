package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Country;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "마이페이지용 방문 국가 목록 응답 DTO")
public class VisitedCountryListResponseDto {

    @Schema(description = "방문한 전체 국가 수", example = "3")
    private Long countryCount;

    @Schema(description = "방문한 국가 목록")
    private List<VisitedCountryItem> visitedCountries;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "방문 국가 단일 정보")
    public static class VisitedCountryItem {

        @Schema(description = "ISO 국가 코드", example = "KR")
        private String countryCode;

        @Schema(description = "방문 국가명", example = "대한민국")
        private String countryName;
    }

    public static VisitedCountryListResponseDto from(
            Long countryCount,
            Collection<Country> countries
    ) {
        List<VisitedCountryItem> items = countries.stream()
                .map(country -> new VisitedCountryItem(
                        country.getCountryCode(),
                        country.getCountryName()
                ))
                .toList();

        return new VisitedCountryListResponseDto(countryCount, items);
    }

    public static VisitedCountryListResponseDto empty() {
        return new VisitedCountryListResponseDto(0L, List.of());
    }
}
