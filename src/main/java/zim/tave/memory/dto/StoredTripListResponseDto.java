package zim.tave.memory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Trip;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "보관된 여행 리스트 응답 DTO")
public class StoredTripListResponseDto {

    @Schema(description = "보관된 여행 목록")
    private List<StoredTripResponseDto> trips;

    public static StoredTripListResponseDto from(List<Trip> trips) {
        return StoredTripListResponseDto.builder()
                .trips(trips.stream().map(StoredTripResponseDto::from).toList())
                .build();
    }
}
