package zim.tave.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zim.tave.memory.domain.User;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.DiaryRepository;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.repository.VisitedCountryRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettingServiceTest {

    private static final Logger log = LoggerFactory.getLogger(SettingServiceTest.class);

    private static final String TEST_IMAGE_URL = "https://example.com/test.jpg";
    private static final String TEST_SURNAME = "sur";
    private static final String TEST_FIRSTNAME = "first";
    private static final String TEST_KOREAN_NAME = "최형원";
    private static final String TEST_NATIONALITY = "KOREA";
    private static final LocalDate TEST_BIRTH = LocalDate.of(2222, 2, 2);
    private static final long INITIAL_COUNT = 0L;
    private static final String INITIAL_FLAGS = "";

    @InjectMocks
    private SettingService settingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VisitedCountryRepository visitedCountryRepository;

    @Mock
    private DiaryRepository diaryRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setKakaoId("testKakaoId");
        testUser.setProfileImageUrl(TEST_IMAGE_URL);
        testUser.setSurName(TEST_SURNAME);
        testUser.setFirstName(TEST_FIRSTNAME);
        testUser.setKoreanName(TEST_KOREAN_NAME);
        testUser.setBirth(TEST_BIRTH);
        testUser.setNationality(TEST_NATIONALITY);
        testUser.setCreatedAt(LocalDate.now());
        testUser.setStatus(true);
        testUser.setRegistered(true);
        testUser.setDiaryCount(INITIAL_COUNT);
        testUser.setVisitedCountryCount(INITIAL_COUNT);
        testUser.setFlags(INITIAL_FLAGS);
    }

    @Test
    public void deleteUser(){
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(visitedCountryRepository).deleteAllByUserId(anyLong());
        doNothing().when(diaryRepository).deleteAllImagesByUserId(anyLong());
        doNothing().when(diaryRepository).deleteAllByUserId(anyLong());
        doNothing().when(userRepository).delete(any(User.class));

        // when
        settingService.deleteAccount(1L);

        // then
        verify(userRepository).delete(testUser);
        log.info("회원 탈퇴 완료");
    }

    @Test
    public void delete_exception() {
        // given
        Long nonExistentId = 99999L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> settingService.deleteAccount(nonExistentId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        log.info("예외 테스트 성공");
    }
}
