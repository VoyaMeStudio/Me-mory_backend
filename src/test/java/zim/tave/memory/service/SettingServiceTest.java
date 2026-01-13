package zim.tave.memory.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.User;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DirtiesContext
public class SettingServiceTest {
    private static final String LOGOUT_KAKAO_ID = "testKakaoId_logout";
    private static final String DELETE_KAKAO_ID = "testKakaoId_delete";
    private static final String TEST_IMAGE_URL = "https://example.com/test.jpg";
    private static final String TEST_SURNAME = "sur";
    private static final String TEST_FIRSTNAME = "first";
    private static final String TEST_KOREAN_NAME = "최형원";
    private static final String TEST_NATIONALITY = "KOREA";
    private static final LocalDate TEST_BIRTH = LocalDate.of(2222, 2, 2);
    private static final long INITIAL_COUNT = 0L;
    private static final String INITIAL_FLAGS = "";

    @Autowired
    private SettingService settingService;

    @Autowired
    private LoginService loginService;  // @Autowired 추가

    @Autowired
    private UserRepository userRepository;

    @Test
    public void logout(){
        //given
        User testUser = new User();
        testUser.setKakaoId(LOGOUT_KAKAO_ID);
        testUser.setProfileImageUrl(TEST_IMAGE_URL);
        testUser.setStatus(true);
        testUser.setCreatedAt(LocalDate.now());
        testUser.setRegistered(true);
        User savedUser = userRepository.save(testUser);

        //when
        loginService.logout(savedUser.getId());

        //then
        User loggedOutUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(loggedOutUser.isStatus()).isFalse();
        System.out.println("로그아웃 성공");
    }

    @Test
    public void deleteUser(){
        //given
        User testUser = new User();
        testUser.setKakaoId(DELETE_KAKAO_ID);
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

        User savedTestUser = userRepository.save(testUser);

        //when
        settingService.deleteAccount(savedTestUser.getId());

        //then
        Optional<User> user = userRepository.findById(savedTestUser.getId());
        assertThat(user).isEmpty();
        System.out.println("회원 탈퇴 완료");
    }

    @Test
    public void delete_exception() {
        //given
        Long testUserId = 99999L;  // 존재하지 않는 ID

        // when & then
        assertThatThrownBy(() -> settingService.deleteAccount(testUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        System.out.println("예외 테스트 성공");
    }
}
