package zim.tave.memory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.domain.AlarmType;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.response.AlarmSendResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.service.AlarmService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users/me/alarm")
public class AlarmController {

    private final AlarmService alarmService;
    private static final String NOTIFICATION_TYPE_KEY = "notificationType";
    private static final String NOTIFICATION_TYPE_NONE = "NONE";

    @PostMapping("/send")
    public ResponseEntity<ApiResponseDto<AlarmSendResponseDto>> sendAlarm(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        AlarmType type = alarmService.decideAlarmType(userId);

        if (type == null) {
            // 전송할 알림 없음 or 알림 수신 거부
            return ResponseEntity.ok(
                    ApiResponseDto.success(
                            ResponseCode.NO_ALARM_TO_SEND,
                            null
                    )
            );
        }

        // 알림 전송 성공
        return ResponseEntity.ok(
                ApiResponseDto.success(
                        ResponseCode.SUCCESS,
                        new AlarmSendResponseDto(type)
                )
        );
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponseDto<AlarmSendResponseDto>> testAlarm(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestParam(required = false) AlarmType alarmType
    ) {
        AlarmType type = alarmType != null
                ? alarmType
                : alarmService.testAlarmType(userId);

        if (type == null) {
            return ResponseEntity.ok(
                    ApiResponseDto.success(
                            ResponseCode.SUCCESS,
                            null
                    )
            );
        }

        return ResponseEntity.ok(
            ApiResponseDto.success(
                    ResponseCode.SUCCESS,
                    new AlarmSendResponseDto(type)
            )
        );
    }
}
