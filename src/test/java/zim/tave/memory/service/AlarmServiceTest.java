package zim.tave.memory.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.AlarmHistory;
import zim.tave.memory.domain.AlarmType;
import zim.tave.memory.domain.Setting;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.domain.User;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.repository.AlarmHistoryRepository;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlarmServiceTest {

    @Mock
    private AlarmPolicyService alarmPolicyService;

    @Mock
    private AlarmHistoryRepository alarmHistoryRepository;

    @Mock
    private TripService tripService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AlarmService alarmService;

    private User userWithSetting(Long userId, boolean alarmOn) {
        User user = new User();
        user.setId(userId);

        Setting setting = mock(Setting.class);
        when(setting.getAlarm()).thenReturn(alarmOn);

        user.setSetting(setting);
        return user;
    }

    private User userWithoutSetting(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setSetting(null);
        return user;
    }

    private Trip trip(Long tripId, LocalDate start, LocalDate end) {
        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setStartDate(start);
        trip.setEndDate(end);
        return trip;
    }

    @Nested
    @DisplayName("decideAlarmType()")
    class DecideAlarmType {

        @Test
        void 유저없으면_USER_NOT_FOUND() {
            // given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(CustomException.class, () -> alarmService.decideAlarmType(userId));

            verify(userRepository).findById(userId);
            verifyNoInteractions(tripService);
            verifyNoInteractions(alarmPolicyService);
            verify(alarmHistoryRepository, never()).save(any());
        }

        @Test
        void setting_null_예외() {
            // given
            Long userId = 1L;
            User user = userWithoutSetting(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // when & then
            assertThrows(CustomException.class, () -> alarmService.decideAlarmType(userId));

            verify(userRepository).findById(userId);
            verifyNoInteractions(tripService);
            verifyNoInteractions(alarmPolicyService);
            verify(alarmHistoryRepository, never()).save(any());
        }

        @Test
        void 알림수신미동의_ALARM_NOT_AGREED() {
            // given
            Long userId = 1L;
            User user = userWithSetting(userId, false);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // when & then
            assertThrows(CustomException.class, () -> alarmService.decideAlarmType(userId));

            verify(userRepository).findById(userId);
            verifyNoInteractions(tripService);
            verifyNoInteractions(alarmPolicyService);
            verify(alarmHistoryRepository, never()).save(any());
        }

        @Test
        void 진행중인_여행_없음() {
            // given
            Long userId = 1L;
            User user = userWithSetting(userId, true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(tripService.findCurrentOngoingTrip(userId)).thenReturn(null);

            // when
            AlarmType result = assertDoesNotThrow(() -> alarmService.decideAlarmType(userId));

            // then
            assertThat(result).isNull();

            verify(userRepository).findById(userId);
            verify(tripService).findCurrentOngoingTrip(userId);
            verifyNoInteractions(alarmPolicyService);
            verify(alarmHistoryRepository, never()).save(any());
        }

        @Test
        void 알림정책_null() {
            // given
            Long userId = 1L;
            Long tripId = 10L;

            User user = userWithSetting(userId, true);
            Trip trip = trip(tripId, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(tripService.findCurrentOngoingTrip(userId)).thenReturn(trip);
            when(alarmPolicyService.decide(user, trip)).thenReturn(null);

            // when
            AlarmType result = alarmService.decideAlarmType(userId);

            // then
            assertThat(result).isNull();

            verify(userRepository).findById(userId);
            verify(tripService).findCurrentOngoingTrip(userId);
            verify(alarmPolicyService).decide(user, trip);
            verify(alarmHistoryRepository, never()).save(any());
        }

        @Test
        void 알림정책_타입_히스토리_저장() {
            // given
            Long userId = 1L;
            Long tripId = 10L;

            User user = userWithSetting(userId, true);
            Trip trip = trip(tripId, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(tripService.findCurrentOngoingTrip(userId)).thenReturn(trip);
            when(alarmPolicyService.decide(user, trip)).thenReturn(AlarmType.DIARY_REMIND);

            ArgumentCaptor<AlarmHistory> captor = ArgumentCaptor.forClass(AlarmHistory.class);

            // when
            AlarmType result = alarmService.decideAlarmType(userId);

            // then
            assertThat(result).isEqualTo(AlarmType.DIARY_REMIND);

            verify(alarmHistoryRepository).save(captor.capture());
            AlarmHistory saved = captor.getValue();

            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getTripId()).isEqualTo(tripId);
            assertThat(saved.getAlarmType()).isEqualTo(AlarmType.DIARY_REMIND);
            assertThat(saved.getSentAt()).isNotNull();
            assertThat(saved.getSentDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("testAlarmType()")
    class TestAlarmType {

        @Test
        void 유저없으면_USER_NOT_FOUND() {
            // given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(CustomException.class, () -> alarmService.testAlarmType(userId));

            verify(userRepository).findById(userId);
            verifyNoInteractions(tripService);
            verifyNoInteractions(alarmPolicyService);
            verify(alarmHistoryRepository, never()).save(any());
        }

        @Test
        void 알림수신미동의_ALARM_NOT_AGREED() {
            // given
            Long userId = 1L;
            User user = userWithSetting(userId, false);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // when & then
            assertThrows(CustomException.class, () -> alarmService.testAlarmType(userId));

            verify(userRepository).findById(userId);
            verifyNoInteractions(tripService);
            verifyNoInteractions(alarmPolicyService);
            verify(alarmHistoryRepository, never()).save(any());
        }

        @Test
        void 진행중인_여행_없음() {
            // given
            Long userId = 1L;
            User user = userWithSetting(userId, true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(tripService.findCurrentOngoingTrip(userId)).thenReturn(null);

            // when
            AlarmType result = alarmService.testAlarmType(userId);

            // then
            assertThat(result).isNull();

            verify(userRepository).findById(userId);
            verify(tripService).findCurrentOngoingTrip(userId);
            verifyNoInteractions(alarmPolicyService);
            verify(alarmHistoryRepository, never()).save(any());
        }

        @Test
        void 여행정책결과_반환() {
            // given
            Long userId = 1L;
            Long tripId = 10L;

            User user = userWithSetting(userId, true);
            Trip trip = trip(tripId, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(tripService.findCurrentOngoingTrip(userId)).thenReturn(trip);
            when(alarmPolicyService.decide(user, trip)).thenReturn(AlarmType.BOARD_DECORATE_REMIND);

            // when
            AlarmType result = alarmService.testAlarmType(userId);

            // then
            assertThat(result).isEqualTo(AlarmType.BOARD_DECORATE_REMIND);

            verify(alarmPolicyService).decide(user, trip);
            verify(alarmHistoryRepository, never()).save(any());
        }
    }
}
