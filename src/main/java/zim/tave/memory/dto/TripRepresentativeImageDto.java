package zim.tave.memory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "여행 대표 이미지 DTO")
public class TripRepresentativeImageDto {

    @Schema(description = "일기 ID", example = "1")
    private Long diaryId;

    @Schema(description = "이미지 ID", example = "1")
    private Long imageId;

    @Schema(description = "이미지 URL", example = "https://image-bucket.s3.amazonaws.com/representative.jpg")
    private String imageUrl;

    @Schema(description = "국가 코드", example = "KR")
    private String countryCode;

    @Schema(description = "국가명", example = "대한민국")
    private String countryName;

    @Schema(description = "도시명", example = "제주시")
    private String city;

    @Schema(description = "일기 내용", example = "오늘은 제주도에서 멋진 하루를 보냈다.")
    private String content;

    public TripRepresentativeImageDto() {}

    public TripRepresentativeImageDto(Long diaryId, Long imageId, String imageUrl, String countryCode,
                                    String countryName, String city, String content) {
        this.diaryId = diaryId;
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.city = city;
        this.content = content;
    }
} 