package zim.tave.memory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Trip;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "보관된 여행 응답 DTO (여행 정보 조회)")
public class StoredTripResponseDto {

    @Schema(description = "여행 고유 ID", example = "1")
    private Long tripId;

    @Schema(description = "여행명", example = "가을 제주 감성여행")
    private String tripName;

    @Schema(description = "여행 테마 ID", example = "1")
    private Long themeId;

    @Schema(description = "여행 대표 이미지 URL", example = "https://s3.amazonaws.com/images/trip1.jpg")
    private String representativeImageUrl;

    public static StoredTripResponseDto from(Trip trip) {
        return StoredTripResponseDto.builder()
                .tripId(trip.getId())
                .tripName(trip.getTripName())
                .themeId(trip.getTripTheme() != null ? trip.getTripTheme().getId() : null)
                .representativeImageUrl(trip.getRepresentativeImageUrl())
                .build();
    }
}
