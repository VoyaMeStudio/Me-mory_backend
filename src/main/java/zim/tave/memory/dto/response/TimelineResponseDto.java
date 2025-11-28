package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import zim.tave.memory.dto.TimelineTripDto;

import java.util.List;

@Getter
@Builder
@Schema(description = "타임라인 응답 DTO")
public class TimelineResponseDto {

    @Schema(description = "타임라인에 표시될 여행 목록")
    private final List<TimelineTripDto> trips;
}

