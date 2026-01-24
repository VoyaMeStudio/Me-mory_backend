package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "일기 응답 DTO")
public class DiaryResponseDto {

	@Schema(description = "일기 ID", example = "1")
	private final Long id;

	@Schema(description = "국가명", example = "대한민국")
	private final String countryName;

	@Schema(description = "국가 이모지", example = "🇰🇷")
	private final String countryEmoji;

	@Schema(description = "도시명", example = "제주시")
	private final String city;

	@Schema(description = "상세 위치", example = "한라산 정상")
	private final String detailedLocation;

	@Schema(description = "일기 작성 날짜 및 시간 (UTC 기준, ISO-8601 형식). 프론트엔드에서는 사용자의 로컬 타임존으로 변환하여 표시해야 합니다.", example = "2024-05-20T10:00:00.000Z")
	private final OffsetDateTime dateTime;

	@Schema(description = "일기 생성 날짜 및 시간 (UTC 기준, ISO-8601 형식). 프론트엔드에서는 사용자의 로컬 타임존으로 변환하여 표시해야 합니다.", example = "2024-05-20T10:00:00.000Z")
	private final OffsetDateTime createdAt;

	@Schema(description = "일기 내용", example = "오늘은 제주도에서 멋진 하루를 보냈다.")
	private final String content;

	@Schema(description = "감정 색상", example = "#FF5733")
	private final String emotionColor;

	@Schema(description = "감정명", example = "행복")
	private final String emotionName;

	@Schema(description = "날씨", example = "맑음")
	private final String weather;

	@Schema(description = "날씨 아이콘 URL", example = "https://example.com/weather/sunny.png")
	private final String weatherIconUrl;

	@Schema(description = "여행 ID", example = "1")
	private final Long tripId;

	@Schema(description = "여행명", example = "제주도 여행")
	private final String tripName;

	@ArraySchema(
			arraySchema = @Schema(description = "이미지 정보 목록"),
			schema = @Schema(implementation = DiaryImageDto.class)
	)
	private final List<DiaryImageDto> images;

	@Getter
	@Builder
	@AllArgsConstructor
	@Schema(description = "일기 이미지 정보")
	public static class DiaryImageDto {

		@Schema(description = "이미지 ID", example = "1")
		private final Long id;

		@Schema(description = "이미지 URL", example = "https://image-bucket.s3.amazonaws.com/front.jpg")
		private final String imageUrl;

		@Schema(description = "카메라 타입", example = "FRONT", allowableValues = {"FRONT", "BACK"})
		private final String cameraType;

		@Schema(description = "대표 이미지 여부", example = "true")
		private final Boolean isRepresentative;

		@Schema(description = "이미지 순서", example = "0")
		private final Integer imageOrder;
	}
}
