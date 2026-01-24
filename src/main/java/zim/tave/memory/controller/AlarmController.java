package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.config.swagger.ApiErrorCodeExamples;
import zim.tave.memory.domain.AlarmType;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.response.AlarmSendResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.service.AlarmService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users/me/alarm")
@Table(
        name = "alarm_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_sent_date",
                        columnNames = {"user_id", "sent_date"}
                )
        }
)
@Tag(name = "alarm-controller", description = "알림 전송")
public class AlarmController {

    private final AlarmService alarmService;
    private static final String NOTIFICATION_TYPE_KEY = "notificationType";
    private static final String NOTIFICATION_TYPE_NONE = "NONE";

    @Operation(
            summary = "알림 전송",
            description = """
            사용자에게 조건에 맞는 알림을 전송합니다.

            - 알림 수신 동의가 되어 있지 않으면 실패합니다.
            - 전송할 알림이 없는 경우 성공 응답(data=null)을 반환합니다.
            - 전송 성공 시 알림 종류를 반환합니다.
            """
    )
    @SecurityRequirements
    @ApiErrorCodeExamples({
            ErrorCode.ALARM_NOT_AGREED,
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 처리 성공 (알림 전송 또는 전송할 알림 없음)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                    잘못된 요청입니다. 다음 오류가 발생할 수 있습니다:
                    - ALARM_NOT_AGREED: 알림 수신 미동의 사용자입니다.
                    """,
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED
                - INVALID_TOKEN
                - UNAUTHORIZED_USER
                """, content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                    리소스를 찾을 수 없습니다. 다음 오류가 발생할 수 있습니다:
                    - USER_NOT_FOUND: 사용자를 찾을 수 없습니다.
                    """,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                    서버 오류입니다. 다음 오류가 발생할 수 있습니다:
                    - INTERNAL_SERVER_ERROR: 알림 전송 처리 중 알 수 없는 오류가 발생했습니다.
                    """,
                    content = @Content
            )
    })
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

    @Operation(
            summary = "알림 테스트 전송",
            description = """
            알림 정책과 무관하게 특정 알림 타입을 강제로 전송하는 테스트용 API입니다.

            - 알림 수신 동의 여부를 무시합니다.
            - 히스토리는 저장되지 않습니다.
            - 개발 및 QA 테스트 용도로만 사용됩니다.
            """
    )
    @SecurityRequirements
    @ApiErrorCodeExamples({
            ErrorCode.ALARM_NOT_AGREED,
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHORIZED_USER,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 테스트 전송 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                    잘못된 요청입니다. 다음 오류가 발생할 수 있습니다:
                    - ALARM_NOT_AGREED: 알림 수신 미동의 사용자입니다.
                    """,
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = """
                인증 실패입니다. 다음 에러 코드가 발생할 수 있습니다:
                - AUTHENTICATION_FAILED
                - INVALID_TOKEN
                - UNAUTHORIZED_USER
                """, content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                    리소스를 찾을 수 없습니다. 다음 오류가 발생할 수 있습니다:
                    - USER_NOT_FOUND: 사용자를 찾을 수 없습니다.
                    """,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                    서버 오류입니다. 다음 오류가 발생할 수 있습니다:
                    - INTERNAL_SERVER_ERROR: 알림 테스트 처리 중 알 수 없는 오류가 발생했습니다.
                    """,
                    content = @Content
            )
    })
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
