package zim.tave.memory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import zim.tave.memory.dto.request.LoginRequestDto;
import zim.tave.memory.dto.response.LoginResponseDto;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.kakao.KakaoApiClient;
import zim.tave.memory.kakao.KakaoUserInfo;
import zim.tave.memory.repository.UserRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;  // 추가

    @InjectMocks
    private LoginService loginService;

    @Test
    void testKakaoLogin() {
        // given
        String testAccessToken = "test-token";
        String testKakaoId = "99999";
        String testImageUrl = "https://test.com/img.jpg";

        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(testKakaoId, testImageUrl);
        Mockito.when(kakaoApiClient.getKakaoUserInfo(testAccessToken))
                .thenReturn(kakaoUserInfo);

        // 새 사용자 생성 시나리오
        Mockito.when(userRepository.findByKakaoId(testKakaoId))
                .thenReturn(java.util.Optional.empty());
        Mockito.when(userRepository.save(Mockito.any(zim.tave.memory.domain.User.class)))
                .thenAnswer(invocation -> {
                    zim.tave.memory.domain.User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });
        Mockito.when(jwtUtil.generateToken(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn("test-jwt-token");

        LoginRequestDto request = new LoginRequestDto(testAccessToken);

        // When
        LoginResponseDto response = loginService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getKakaoId()).isEqualTo("99999");
        assertThat(response.getProfileImageUrl()).isEqualTo("https://test.com/img.jpg");

        System.out.println("카카오 ID: " + response.getKakaoId());
        System.out.println("프로필 이미지: " + response.getProfileImageUrl());
    }
}
