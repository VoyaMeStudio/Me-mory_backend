package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.*;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.AlarmHistoryRepository;
import zim.tave.memory.repository.SettingRepository;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmPolicyService alarmPolicyService;
    private final AlarmHistoryRepository alarmHistoryRepository;
    private final TripService tripService;
    private final UserRepository userRepository;

    @Transactional
    public AlarmType decideAlarmType(Long userId) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //1. 알림 수신 동의 확인
        if (user.getSetting() == null || Boolean.FALSE.equals(user.getSetting().getAlarm())) {
            throw new CustomException(ErrorCode.ALARM_NOT_AGREED);
        }

        // 2. 여행중 여부 확인
        Trip trip = tripService.findCurrentOngoingTrip(user.getId());
        if (trip == null) {
            return null;
        }

        // 3. 전송할 알림 타입 결정
        AlarmType type = alarmPolicyService.decide(user, trip);
        if (type == null) {
            return null;
        }

        // 4. 알림 전송 히스토리에 저장
        alarmHistoryRepository.save(
                AlarmHistory.create(
                        userId,
                        trip.getId(),
                        type,
                        LocalDateTime.now()
                )
        );

        // 전송할 알림 타입 결정
        return type;

    }

    // 알림호출 테스트 (히스토리 저장X)
    @Transactional(readOnly = true)
    public AlarmType testAlarmType(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.getSetting().getAlarm()) {
            throw new CustomException(ErrorCode.ALARM_NOT_AGREED);
        }

        Trip trip = tripService.findCurrentOngoingTrip(userId);
        if (trip == null) {
            return null;
        }

        return alarmPolicyService.decide(user, trip);
    }

}
