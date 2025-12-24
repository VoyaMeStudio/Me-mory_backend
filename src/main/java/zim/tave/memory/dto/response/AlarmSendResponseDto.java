package zim.tave.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.domain.AlarmType;

public class AlarmSendResponseDto {

    @Schema(description = "알림 종류", example = "DIARY_REMIND")
    private String notificationType;

    public AlarmSendResponseDto(AlarmType type) {
        this.notificationType = type.name();
    }
}
