package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zim.tave.memory.domain.Diary;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "보관된 일기 목록 응답 DTO")
public class StoredDiaryListResponseDto {

    @Schema(description = "보관된 일기 목록")
    private List<StoredDiaryResponseDto> storedDiaries;

    public static StoredDiaryListResponseDto from(List<Diary> diaries) {
        return new StoredDiaryListResponseDto(
                diaries.stream()
                        .map(StoredDiaryResponseDto::from)
                        .toList()
        );
    }
}
