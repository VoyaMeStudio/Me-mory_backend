package zim.tave.memory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 타임라인에서 표시되는 여행 단위 DTO
 * 여행의 기본 정보(ID, 이름, 설명, 날짜 등)와 가장 최근 일기의 감정 정보(색상 및 이름), 대표 이미지 URL, 방문한 국가 목록(감정 정보 제외)을 포함합니다.
 */
@Getter
@Builder
@Schema(description = "타임라인 여행 단위 DTO")
public class TimelineTripDto {

    @Schema(description = "여행 ID", example = "42")
    private final Long tripId;

    @Schema(description = "여행 이름", example = "봄 제주 여행")
    private final String tripName;

    @Schema(description = "여행 설명", example = "벚꽃 구경")
    private final String description;

    @Schema(description = "여행 시작일", example = "2025-03-20")
    private final LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2025-03-24")
    private final LocalDate endDate;

    @Schema(description = "과거 여행 여부", example = "true")
    private final boolean isPast;

    @Schema(description = "대표 이미지 URL", example = "https://cdn.example.com/trip-42.jpg")
    private final String representativeImageUrl;

    @Schema(description = "감정명 (가장 최근 일기의 감정)", example = "설렘")
    private final String emotionName;

    @Schema(description = "감정 색상 코드 (가장 최근 일기의 감정)", example = "#FF6B6B")
    private final String emotionColor;

    @Schema(description = "방문한 국가 정보 목록")
    private final List<VisitedCountryInfoDto> visitedCountries;
}

