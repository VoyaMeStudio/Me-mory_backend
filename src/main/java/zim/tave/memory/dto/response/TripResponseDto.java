package zim.tave.memory.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TripResponseDto {

	private final Long id;
	private final String tripName;
	private final String description;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final String representativeImageUrl;

	// 간단한 사용자 정보
	private final Long userId;
	private final String userKakaoId;
	private final String userKoreanName;

	// 테마 정보
	private final Long themeId;
	private final String themeName;
	private final String themeSampleImageUrl;
	private final String themeCardImageUrl;

	// 다이어리 개수
	private final int diaryCount;
}
