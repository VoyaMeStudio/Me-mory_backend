package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Diary;
import zim.tave.memory.domain.DiaryImage;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "보관된 일기 응답 DTO")
public class StoredDiaryResponseDto {

    @Schema(description = "일기 고유 ID", example = "10")
    private Long diaryId;

    @Schema(description = "일기 대표 이미지 URL", example = "https://s3.amazonaws.com/images/diary1.jpg")
    private String representativeImageUrl;

    @Schema(description = "일기 작성 날짜", example = "2024-11-14T18:20:00")
    private LocalDateTime createdAt;

    @Schema(description = "작성 요일", example = "THURSDAY")
    private String dayOfWeek;

    public static StoredDiaryResponseDto from(Diary diary) {

        // 대표 이미지 찾기
        String representativeUrl = diary.getDiaryImages().stream()
                .filter(DiaryImage::isRepresentative)
                .map(DiaryImage::getImageUrl)
                .findFirst()
                .orElse(null);

        LocalDateTime created = diary.getCreatedAt();
        String day = created.getDayOfWeek().name();

        return StoredDiaryResponseDto.builder()
                .diaryId(diary.getId())
                .representativeImageUrl(representativeUrl)
                .createdAt(created)
                .dayOfWeek(day)
                .build();
    }
}
