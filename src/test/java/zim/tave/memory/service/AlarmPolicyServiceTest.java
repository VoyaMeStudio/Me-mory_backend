package zim.tave.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.AlarmType;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.domain.User;
import zim.tave.memory.repository.AlarmHistoryRepository;
import zim.tave.memory.repository.DiaryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlarmPolicyServiceTest {

    @Mock
    private AlarmHistoryRepository alarmHistoryRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @InjectMocks
    private AlarmPolicyService alarmPolicyService;

    private User user;
    private Trip trip;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        trip = new Trip();
        trip.setId(10L);

        // 기본값: 12일 여행(슬롯 계산에서 DIARY_COMPLETE_REMIND 조건 만들기 위해 필요)
        // totalTripDays = 12 (inclusive), serviceSlotLimit=floor(12*0.25)=3, maxIncompleteSlot=floor(3*0.4)=1
        trip.setStartDate(LocalDate.of(2026, 1, 1));
        trip.setEndDate(LocalDate.of(2026, 1, 12));
    }

    @Test
    void 알림전송이력있으면_null반환() {
        // given
        when(alarmHistoryRepository.existsByUserIdAndSentDate(eq(user.getId()), any(LocalDate.class)))
                .thenReturn(true);

        // when
        AlarmType result = alarmPolicyService.decide(user, trip);

        // then
        assertThat(result).isNull();

        // 하루 캡에서 막히면 아래 로직으로 진행되면 안 됨
        verify(alarmHistoryRepository, never()).countByUserIdAndSentAtAfter(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void 일주일내_3회이상_전송시_null반환() {
        // given
        when(alarmHistoryRepository.existsByUserIdAndSentDate(eq(user.getId()), any(LocalDate.class)))
                .thenReturn(false);

        when(alarmHistoryRepository.countByUserIdAndSentAtAfter(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(3L);

        // when
        AlarmType result = alarmPolicyService.decide(user, trip);

        // then
        assertThat(result).isNull();
    }

    @Test
    void DIARY_COMPLETE_REMIND_전송() {
        // given: 캡 통과
        when(alarmHistoryRepository.existsByUserIdAndSentDate(eq(user.getId()), any(LocalDate.class)))
                .thenReturn(false);
        when(alarmHistoryRepository.countByUserIdAndSentAtAfter(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(0L);

        // used counts: 0/0 => usedService+usedIncomplete < 3 만족, usedIncomplete < 1 만족(0 < 1)
        when(alarmHistoryRepository.countByTripIdAndAlarmType(eq(trip.getId()), eq(AlarmType.BOARD_DECORATE_REMIND)))
                .thenReturn(0L);
        when(alarmHistoryRepository.countByTripIdAndAlarmType(eq(trip.getId()), eq(AlarmType.DIARY_COMPLETE_REMIND)))
                .thenReturn(0L);

        // 미완성 존재
        when(diaryRepository.existsIncompleteDiaryByTripId(eq(trip.getId())))
                .thenReturn(true);

        // 쿨다운 통과 (private isInCooldown 내부에서 repository 호출)
        when(alarmHistoryRepository.existsByUserIdAndAlarmTypeAndSentAtAfter(eq(user.getId()), eq(AlarmType.DIARY_COMPLETE_REMIND), any(LocalDateTime.class)))
                .thenReturn(false);

        // when
        AlarmType result = alarmPolicyService.decide(user, trip);

        // then
        assertThat(result).isEqualTo(AlarmType.DIARY_COMPLETE_REMIND);
    }

    @Test
    void BOARD_DECORATE_REMIND_전송() {
        // given: 캡 통과
        when(alarmHistoryRepository.existsByUserIdAndSentDate(eq(user.getId()), any(LocalDate.class)))
                .thenReturn(false);
        when(alarmHistoryRepository.countByUserIdAndSentAtAfter(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(0L);

        // 미완성 없음 -> DIARY_COMPLETE 분기 스킵
        when(diaryRepository.existsIncompleteDiaryByTripId(eq(trip.getId())))
                .thenReturn(false);

        // used counts < serviceSlotLimit 만족
        when(alarmHistoryRepository.countByTripIdAndAlarmType(eq(trip.getId()), eq(AlarmType.BOARD_DECORATE_REMIND)))
                .thenReturn(0L);
        when(alarmHistoryRepository.countByTripIdAndAlarmType(eq(trip.getId()), eq(AlarmType.DIARY_COMPLETE_REMIND)))
                .thenReturn(0L);

        // 보드 쿨다운 통과
        when(alarmHistoryRepository.existsByUserIdAndAlarmTypeAndSentAtAfter(eq(user.getId()), eq(AlarmType.BOARD_DECORATE_REMIND), any(LocalDateTime.class)))
                .thenReturn(false);

        // when
        AlarmType result = alarmPolicyService.decide(user, trip);

        // then
        assertThat(result).isEqualTo(AlarmType.BOARD_DECORATE_REMIND);
    }

    @Test
    void DIARY_REMIND_전송() {
        // given: 캡 통과
        when(alarmHistoryRepository.existsByUserIdAndSentDate(eq(user.getId()), any(LocalDate.class)))
                .thenReturn(false);
        when(alarmHistoryRepository.countByUserIdAndSentAtAfter(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(0L);

        // 미완성 없음
        when(diaryRepository.existsIncompleteDiaryByTripId(eq(trip.getId())))
                .thenReturn(false);

        // 슬롯을 꽉 채워서 보드 조건 실패시키기:
        // serviceSlotLimit=3 이므로 usedService+usedIncomplete=3이면 < 3 조건 실패
        when(alarmHistoryRepository.countByTripIdAndAlarmType(eq(trip.getId()), eq(AlarmType.BOARD_DECORATE_REMIND)))
                .thenReturn(2L);
        when(alarmHistoryRepository.countByTripIdAndAlarmType(eq(trip.getId()), eq(AlarmType.DIARY_COMPLETE_REMIND)))
                .thenReturn(1L);

        // diary remind 쿨다운 통과
        when(alarmHistoryRepository.existsByUserIdAndAlarmTypeAndSentAtAfter(eq(user.getId()), eq(AlarmType.DIARY_REMIND), any(LocalDateTime.class)))
                .thenReturn(false);

        // when
        AlarmType result = alarmPolicyService.decide(user, trip);

        // then
        assertThat(result).isEqualTo(AlarmType.DIARY_REMIND);
    }

    @Test
    void 모든조건통과못하면_null() {
        // given: 캡 통과
        when(alarmHistoryRepository.existsByUserIdAndSentDate(eq(user.getId()), any(LocalDate.class)))
                .thenReturn(false);
        when(alarmHistoryRepository.countByUserIdAndSentAtAfter(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(0L);

        // 미완성 없음
        when(diaryRepository.existsIncompleteDiaryByTripId(eq(trip.getId())))
                .thenReturn(false);

        // 보드 조건 실패(슬롯 꽉)
        when(alarmHistoryRepository.countByTripIdAndAlarmType(eq(trip.getId()), eq(AlarmType.BOARD_DECORATE_REMIND)))
                .thenReturn(2L);
        when(alarmHistoryRepository.countByTripIdAndAlarmType(eq(trip.getId()), eq(AlarmType.DIARY_COMPLETE_REMIND)))
                .thenReturn(1L);

        // DIARY_REMIND도 쿨다운 => null
        when(alarmHistoryRepository.existsByUserIdAndAlarmTypeAndSentAtAfter(eq(user.getId()), eq(AlarmType.DIARY_REMIND), any(LocalDateTime.class)))
                .thenReturn(true);

        // when
        AlarmType result = alarmPolicyService.decide(user, trip);

        // then
        assertThat(result).isNull();
    }
}
