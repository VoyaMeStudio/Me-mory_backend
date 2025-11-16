package zim.tave.memory.dto;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DiaryResponseDto {
	
	private final Long id;
	private final String countryName;
	private final String countryEmoji;
	private final String city;
	private final String detailedLocation;
	private final LocalDateTime dateTime;
	private final LocalDateTime createdAt;
	private final String content;
	private final String audioUrl;
	private final String emotionColor;
	private final String emotionName;
	private final String weather;
	private final String weatherIconUrl;
	
	// 간단한 여행 정보
	private final Long tripId;
	private final String tripName;
	
	// 이미지 정보
	private final List<DiaryImageDto> images;
	
	@Getter
	@Builder
	@AllArgsConstructor
	public static class DiaryImageDto {
		private final Long id;
		private final String imageUrl;
		private final String cameraType;
		private final Boolean isRepresentative;
		private final Integer imageOrder;
	}
} 