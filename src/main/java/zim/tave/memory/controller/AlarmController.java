package zim.tave.memory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.domain.AlarmType;
import zim.tave.memory.domain.User;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.service.AlarmService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/me/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponseDto<?>> sendAlarm(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        AlarmType type = alarmService.decideAlarmType(userId);

        if (type == null) {
            // 전송할 알림 없음 or 알림 수신 거부
            return ResponseEntity.ok(
                    ApiResponseDto.success(
                            ResponseCode.SUCCESS,
                            null
                    )
            );
        }

        // 알림 전송 성공
        return ResponseEntity.ok(
                ApiResponseDto.success(
                        ResponseCode.SUCCESS,
                        Map.of("notificationType", type.name())
                )
        );
    }
}
