package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.AlarmHistory;
import zim.tave.memory.domain.AlarmType;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.domain.User;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.AlarmHistoryRepository;
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
            return null;
        }

        // 2. 여행중 여부 확인
        Trip currentTrip = tripService.findCurrentOngoingTrip(user.getId());
        if (currentTrip == null) {
            return null;
        }

        // 3. 전송할 알림 타입 결정
        AlarmType type = alarmPolicyService.decide(user, currentTrip);
        if (type == null) {
            return null;
        }

        // 4. 알림 전송 히스토리에 저장
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        alarmHistoryRepository.save(
                AlarmHistory.create(user.getId(), currentTrip.getId(), type, now)
        );

        // 전송할 알림 타입 결정
        return type;

    }

}
