package zim.tave.memory.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zim.tave.memory.domain.*;
import zim.tave.memory.dto.request.UpdateUserRequestDto;
import zim.tave.memory.dto.response.UserResponseDto;
import zim.tave.memory.service.DiaryService;
import zim.tave.memory.service.LoginService;
import zim.tave.memory.service.SettingService;
import zim.tave.memory.service.StorageService;
import zim.tave.memory.service.TripService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 설정 관리 통합 테스트
 * - 개인 정보 수정
 * - 보관된 여행/일기 관리 (복구/삭제)
 * - 로그아웃
 * - 회원 탈퇴
 */
public class SettingsIntegrationTest extends IntegrationTestBase {

    @Autowired
    private SettingService settingService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private TripService tripService;

    @Autowired
    private DiaryService diaryService;

    private Trip testTrip;
    private Diary testDiary;

    @BeforeEach
    void setUp() {
        initializeTestData();

        // 테스트용 여행 및 일기 생성
        testTrip = createTestTrip(
            "제주도 여행",
            "제주도 여행",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 4)
        );

        testDiary = createTestDiary(
            testTrip,
            "제주시",
            OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC),
            "일기 내용"
        );
    }

    @Test
    void 개인_정보_수정_테스트() {
        // given
        UpdateUserRequestDto request = UpdateUserRequestDto.builder()
            .surName("UPDATED")
            .firstName("NAME")
            .koreanName("업데이트된 이름")
            .birth(LocalDate.of(2000, 1, 1))
            .nationality("UNITED STATES")
            .build();

        // when
        UserResponseDto updatedUser = settingService.updateUserInfo(testUser.getId(), request);

        // then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getKoreanName()).isEqualTo("업데이트된 이름");
        assertThat(updatedUser.getSurName()).isEqualTo("UPDATED");
        assertThat(updatedUser.getFirstName()).isEqualTo("NAME");

        User user = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(user.getKoreanName()).isEqualTo("업데이트된 이름");
    }

    @Test
    void 보관된_여행_목록_조회_테스트() {
        // given - 여행 보관
        tripService.storeTrip(testTrip.getId(), testUser.getId(), true);

        // when
        var storedTrips = storageService.getStoredTrips(testUser.getId());

        // then
        assertThat(storedTrips).isNotNull();
        assertThat(storedTrips.getTrips()).hasSize(1);
        assertThat(storedTrips.getTrips().get(0).getTripName()).isEqualTo("제주도 여행");
    }

    @Test
    void 보관된_여행_복구_테스트() {
        // given - 여행 보관
        tripService.storeTrip(testTrip.getId(), testUser.getId(), true);

        // when - 보관 해제
        storageService.unstoreTrips(testUser.getId(), Arrays.asList(testTrip.getId()));

        // then
        Trip restoredTrip = tripRepository.findById(testTrip.getId()).orElseThrow();
        assertThat(restoredTrip.getIsStored()).isFalse();

        // 일기도 함께 복구되었는지 확인
        List<Diary> diaries = diaryRepository.findByTripId(testTrip.getId());
        diaries.forEach(diary -> assertThat(diary.getIsStored()).isFalse());
    }

    @Test
    void 보관된_여행_삭제_테스트() {
        // given - 여행 보관
        tripService.storeTrip(testTrip.getId(), testUser.getId(), true);

        // when - 보관된 여행 삭제
        storageService.deleteStoredTrips(testUser.getId(), Arrays.asList(testTrip.getId()));

        // then
        assertThat(tripRepository.findById(testTrip.getId())).isEmpty();
    }

    @Test
    void 보관된_일기_목록_조회_테스트() {
        // given - 일기 보관
        diaryService.storeDiary(testDiary.getId(), testUser.getId(), true);

        // when
        var storedDiaries = storageService.getStoredDiaries(testUser.getId());

        // then
        assertThat(storedDiaries).isNotNull();
    }

    @Test
    void 보관된_일기_복구_테스트() {
        // given - 일기 보관
        diaryService.storeDiary(testDiary.getId(), testUser.getId(), true);

        // when - 보관 해제
        storageService.restoreStoredDiaries(testUser.getId(), Arrays.asList(testDiary.getId()));

        // then
        Diary restoredDiary = diaryRepository.findById(testDiary.getId()).orElseThrow();
        assertThat(restoredDiary.getIsStored()).isFalse();
    }

    @Test
    void 보관된_일기_삭제_테스트() {
        // given - 일기 보관
        diaryService.storeDiary(testDiary.getId(), testUser.getId(), true);

        // when - 보관된 일기 삭제
        storageService.deleteStoredDiaries(testUser.getId(), Arrays.asList(testDiary.getId()));

        // then
        assertThat(diaryRepository.findById(testDiary.getId())).isEmpty();
    }

    @Test
    void 로그아웃_테스트() {
        // given
        testUser.setStatus(true);
        userRepository.save(testUser);

        // when
        loginService.logout(testUser.getId());

        // then
        User loggedOutUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(loggedOutUser.isStatus()).isFalse();
    }

    @Test
    void 회원_탈퇴_테스트() {
        // when
        settingService.deleteAccount(testUser.getId());

        // then
        assertThat(userRepository.findById(testUser.getId())).isEmpty();
        
        // 연관 데이터도 삭제되었는지 확인
        assertThat(tripRepository.findByUserId(testUser.getId())).isEmpty();
        assertThat(diaryRepository.findByUser_Id(testUser.getId())).isEmpty();
    }
}

