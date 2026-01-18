package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import zim.tave.memory.domain.AlarmType;

@Getter
@Schema(description = "알림 전송 결과 DTO")
public class AlarmSendResponseDto {

    @Schema(
            description = "전송된 알림 타입",
            example = "BOARD_DECORATE_REMIND",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String notificationType;

    public AlarmSendResponseDto(AlarmType type) {
        this.notificationType = type.name();
    }
}
