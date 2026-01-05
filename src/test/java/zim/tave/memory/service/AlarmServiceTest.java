package zim.tave.memory.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.*;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.repository.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
public class AlarmServiceTest {

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private AlarmHistoryRepository alarmHistoryRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    private User createUser(boolean alarmAgree) {
        User user = new User();
        user.setKakaoId("test-kakao");
        user.setRegistered(true);
        userRepository.save(user);

        Setting setting = new Setting();
        setting.setUser(user);
        setting.setAlarm(alarmAgree);
        settingRepository.save(setting);

        return user;
    }

    @Test
    void boardDecorateRemind_success() {
        User user = createUser(true);

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setStartDate(LocalDate.now().minusDays(1));
        trip.setEndDate(LocalDate.now().plusDays(3));
        tripRepository.save(trip);

        AlarmType type = alarmService.decideAlarmType(user.getId());

        assertThat(type).isEqualTo(AlarmType.BOARD_DECORATE_REMIND);
    }

    /*
    @Test
    void diaryCompleteRemind_whenIncompleteDiaryExists() {
        User user = createUser(true);

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setStartDate(LocalDate.now().minusDays(2));
        trip.setEndDate(LocalDate.now().plusDays(2));
        tripRepository.save(trip);

        Diary diary = new Diary();
        diary.setTrip(trip);
        diary.setCompleted(false);
        diaryRepository.save(diary);

        AlarmType type = alarmService.decideAlarmType(user.getId());

        assertThat(type).isEqualTo(AlarmType.DIARY_COMPLETE_REMIND);
    }

     */

    @Test
    void diaryRemind_defaultFallback() {
        User user = createUser(true);

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setStartDate(LocalDate.now().minusDays(1));
        trip.setEndDate(LocalDate.now().plusDays(1));
        tripRepository.save(trip);

        AlarmType type = alarmService.decideAlarmType(user.getId());

        assertThat(type).isEqualTo(AlarmType.DIARY_REMIND);
    }

    @Test
    void alarmBlocked_whenUserDisagreed() {
        User user = createUser(false);

        assertThatThrownBy(() ->
                alarmService.decideAlarmType(user.getId())
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("알림 수신 미동의");
    }

    /*
    @Test
    void alarmHistory_isSaved() {
        User user = createUser(true);

        AlarmType type = alarmService.decideAlarmType(user.getId());

        List<AlarmHistory> histories =
                alarmHistoryRepository.findAllByUserId(user.getId());

        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getAlarmType()).isEqualTo(type);
    }

     */
}
