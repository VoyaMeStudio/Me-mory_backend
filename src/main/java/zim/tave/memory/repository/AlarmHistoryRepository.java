package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zim.tave.memory.domain.AlarmHistory;
import zim.tave.memory.domain.AlarmType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AlarmHistoryRepository extends JpaRepository<AlarmHistory, Long> {

    // 하루 캡 (1일 1회): 오늘 보낼 수 있는 알림 횟수
    boolean existsByUserIdAndSentDate(Long userId, LocalDate sentDate);

    // 주간 캡 (최근 N일): 이번주 보낼 수 있는 알림 횟수
    long countByUserIdAndSentAtAfter(Long userId, LocalDateTime from);

    // 타입별 쿨다운
    boolean existsByUserIdAndAlarmTypeAndSentAtAfter(
            Long userId,
            AlarmType alarmType,
            LocalDateTime after
    );

    // 여행 중 알림 카운트
    long countByTripId(Long tripId);

    long countByTripIdAndAlarmType(Long tripId, AlarmType alarmType);

    // 최근 알림 조회
    List<AlarmHistory> findTop10ByUserIdOrderBySentAtDesc(Long userId);
}
