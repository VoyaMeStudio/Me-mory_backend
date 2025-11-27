package zim.tave.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.JoinRequestDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JoinServiceTest {

    @InjectMocks
    private JoinService joinService;

    @Mock
    private UserRepository userRepository;

    @Test
    void 회원가입_성공() {
        // given
        User user = new User();
        user.setId(1L);
        user.setKakaoId("kakao123");
        user.setRegistered(false);

        JoinRequestDto dto = new JoinRequestDto(
                "KANG", "JIHYE", "강지혜",
                LocalDate.of(2000, 1, 1),
                "REPUBLIC OF KOREA"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // when
        User result = joinService.join(1L, dto);

        // then
        assertThat(result.isRegistered()).isTrue();
        assertThat(result.getFirstName()).isEqualTo("JIHYE");
        assertThat(result.getSurName()).isEqualTo("KANG");
        assertThat(result.getBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    void 유저없음_USER_NOT_FOUND() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() ->
                joinService.join(999L, new JoinRequestDto())
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 카카오로그인_안됨() {

        //given
        User user = new User();
        user.setId(1L);
        user.setKakaoId(null);
        user.setRegistered(false);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        //when & then
        assertThatThrownBy(() ->
                joinService.join(1L, new JoinRequestDto())
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.KAKAO_LOGIN_REQUIRED.getMessage());
    }

    @Test
    void 이미가입됨_ALREADY_JOINED() {
        // given
        User user = new User();
        user.setId(1L);
        user.setKakaoId("kakao123");
        user.setRegistered(true);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() ->
                joinService.join(1L, new JoinRequestDto())
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_JOINED.getMessage());
    }

}
