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
        testUser.setKakaoId("testKakaoId_logout");
        testUser.setProfileImageUrl("https://example.com/test.jpg");
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
        testUser.setKakaoId("testKakaoId_delete");
        testUser.setProfileImageUrl("https://example.com/test.jpg");
        testUser.setSurName("sur");
        testUser.setFirstName("first");
        testUser.setKoreanName("최형원");
        testUser.setBirth(LocalDate.of(2222, 2, 2));
        testUser.setNationality("KOREA");
        testUser.setCreatedAt(LocalDate.now());
        testUser.setStatus(true);
        testUser.setRegistered(true);
        testUser.setDiaryCount(0L);
        testUser.setVisitedCountryCount(0L);
        testUser.setFlags("");

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
