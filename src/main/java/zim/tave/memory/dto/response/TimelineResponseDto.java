package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import zim.tave.memory.dto.TimelineTripDto;

import java.util.List;

/**
 * 타임라인 조회 API 응답 DTO(사용자의 여행 목록을 타임라인 형태로 제공)
 */
@Getter
@Builder
@Schema(description = "타임라인 응답 DTO")
public class TimelineResponseDto {

    @Schema(description = "타임라인에 표시될 여행 목록 (시작일 기준 내림차순 정렬)")
    private final List<TimelineTripDto> trips;
}

