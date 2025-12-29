package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zim.tave.memory.domain.AlarmType;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.domain.User;
import zim.tave.memory.repository.AlarmHistoryRepository;
import zim.tave.memory.repository.DiaryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AlarmPolicyService {

    private final AlarmHistoryRepository alarmHistoryRepository;
    private final DiaryRepository diaryRepository;

    public AlarmType decide(User user, Trip trip) {

        Long userId = user.getId();
        Long tripId = trip.getId();

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDate today = now.toLocalDate();

        // 하루 캡 (하루 1번 한정)
        if (alarmHistoryRepository.existsByUserIdAndSentDate(userId, today)) {
            return null;
        }

        // 주간 캡 (최근 7일, 최대 3회)
        long weeklyCount =
                alarmHistoryRepository.countByUserIdAndSentAtAfter(
                        userId, now.minusDays(7)
                );

        if (weeklyCount >= 3) {
            return null;
        }

        // 여행 중 전체 슬롯 계산
        long totalTripDays =
                ChronoUnit.DAYS.between(
                        trip.getStartDate(),
                        trip.getEndDate()
                ) + 1;

        long serviceSlotLimit = Math.max(1, (long) Math.floor(totalTripDays * 0.25));
        long maxIncompleteSlot = (long) Math.floor(serviceSlotLimit * 0.4);

        long usedServiceCount =
                alarmHistoryRepository.countByTripIdAndAlarmType(
                        tripId, AlarmType.BOARD_DECORATE_REMIND
                );

        long usedIncompleteCount =
                alarmHistoryRepository.countByTripIdAndAlarmType(
                        tripId, AlarmType.DIARY_COMPLETE_REMIND
                );

        // 미완성 기록 여부
        boolean hasIncompleteDiary =
                diaryRepository.existsIncompleteDiaryByTripId(tripId);

        // 3. 미완성 알림 판단
        if (hasIncompleteDiary
                && usedServiceCount + usedIncompleteCount < serviceSlotLimit
                && usedIncompleteCount < maxIncompleteSlot
                && !isInCooldown(userId, AlarmType.DIARY_COMPLETE_REMIND, now)) {

            return AlarmType.DIARY_COMPLETE_REMIND;
        }

        // 2. 보드 꾸미기 알림 판단
        if (usedServiceCount + usedIncompleteCount < serviceSlotLimit
                && !isInCooldown(userId, AlarmType.BOARD_DECORATE_REMIND, now)) {

            return AlarmType.BOARD_DECORATE_REMIND;
        }

        // 기본 기록 알림 (default)
        if (!isInCooldown(userId, AlarmType.DIARY_REMIND, now)) {
            return AlarmType.DIARY_REMIND;
        }

        return null;
    }

    //공통 쿨다운 체크
    private boolean isInCooldown(Long userId, AlarmType type, LocalDateTime now) {
        return alarmHistoryRepository
                .existsByUserIdAndAlarmTypeAndSentAtAfter(
                        userId,
                        type,
                        now.minusHours(48)
                );
    }
}
