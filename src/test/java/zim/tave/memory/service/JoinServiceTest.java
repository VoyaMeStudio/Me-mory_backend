package zim.tave.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.JoinRequestDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JoinServiceTest {

    private static final String TEST_KAKAO_ID = "testKakaoId000";
    private static final String TEST_IMAGE_URL = "https://test.com/image.jpg";
    private static final String TEST_SURNAME = "sur";
    private static final String TEST_FIRSTNAME = "first";
    private static final String TEST_KOREAN_NAME = "최형원";
    private static final String TEST_NATIONALITY = "REPUBLIC OF KOREA";
    private static final LocalDate TEST_BIRTH = LocalDate.of(2222, 2, 2);
    private static final long INITIAL_COUNT = 0L;
    private static final String INITIAL_FLAGS = "";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JoinService joinService;

    private User user;
    private JoinRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setKakaoId("testKakaoId000");
        user.setProfileImageUrl("https://test.com/image.jpg");
        user.setRegistered(false);

        requestDto = new JoinRequestDto(
                TEST_SURNAME,
                TEST_FIRSTNAME,
                TEST_KOREAN_NAME,
                TEST_BIRTH,
                TEST_NATIONALITY,
                TEST_IMAGE_URL,
                true
        );
    }

    @Test
    void 회원가입_성공_테스트() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User savedUser = joinService.join(1L, requestDto);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getKakaoId()).isEqualTo(TEST_KAKAO_ID);
        assertThat(savedUser.getProfileImageUrl()).isEqualTo(TEST_IMAGE_URL);
        assertThat(savedUser.getSurName()).isEqualTo(TEST_SURNAME);
        assertThat(savedUser.getFirstName()).isEqualTo(TEST_FIRSTNAME);
        assertThat(savedUser.getKoreanName()).isEqualTo(TEST_KOREAN_NAME);
        assertThat(savedUser.getBirth()).isEqualTo(TEST_BIRTH);
        assertThat(savedUser.getNationality()).isEqualTo(TEST_NATIONALITY);
        assertThat(savedUser.getDiaryCount()).isEqualTo(INITIAL_COUNT);
        assertThat(savedUser.getVisitedCountryCount()).isEqualTo(INITIAL_COUNT);
        assertThat(savedUser.getFlags()).isEqualTo(INITIAL_FLAGS);
        assertThat(savedUser.isRegistered()).isTrue();
        assertThat(savedUser.isStatus()).isTrue();

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void 사용자_없음_예외_테스트() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> joinService.join(1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void 카카오_로그인_필수_예외_테스트() {
        // given
        user.setKakaoId(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> joinService.join(1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.KAKAO_LOGIN_REQUIRED);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void 이미_가입된_회원_예외_테스트() {
        // given
        user.setRegistered(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> joinService.join(1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_JOINED);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
}
