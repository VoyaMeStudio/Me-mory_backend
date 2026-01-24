package zim.tave.memory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class AlarmHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_history_id")
    private Long id;

    // 알림을 받을 사용자
    @Column(nullable = false)
    private Long userId;

    // 어떤 여행에 대한 알림인지
    @Column(nullable = false)
    private Long tripId;

    // 알림 종류
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlarmType alarmType;

    // 실제 발송 시각
    @Column(nullable = false)
    private LocalDateTime sentAt;

    // 일 단위 조회 최적화를 위한 필드 (캡 계산용)
    @Column(nullable = false)
    private LocalDate sentDate;

    public static AlarmHistory create(
            Long userId,
            Long tripId,
            AlarmType alarmType,
            LocalDateTime sentAt
    ) {
        AlarmHistory history = new AlarmHistory();
        history.userId = userId;
        history.tripId = tripId;
        history.alarmType = alarmType;
        history.sentAt = sentAt;
        history.sentDate = sentAt.toLocalDate();
        return history;
    }
}
