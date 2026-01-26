package zim.tave.memory.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.LoginRequestDto;
import zim.tave.memory.dto.response.LoginResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.kakao.KakaoApiClient;
import zim.tave.memory.kakao.KakaoUserInfo;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final KakaoApiClient kakaoApiClient;
    private final JwtUtil jwtUtil;

    public LoginResponseDto login(LoginRequestDto request) {

        if (request.getAccessToken() == null || request.getAccessToken().isBlank()) {
            throw new CustomException(ErrorCode.KAKAO_TOKEN_MISSING);
        }

        // 카카오 액세스 토큰으로 사용자 정보 조회
        KakaoUserInfo kakaoUserInfo = kakaoApiClient.getKakaoUserInfo(request.getAccessToken());
        User user = userRepository.findByKakaoId(kakaoUserInfo.getKakaoId()).orElse(null);

        // 기존 사용자
        if (user != null) {
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getKakaoId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());
            return LoginResponseDto.from(user, accessToken, refreshToken, user.isRegistered());
        }

        // 처음 로그인한 사용자 → User 생성
        User newUser = new User();
        newUser.setKakaoId(kakaoUserInfo.getKakaoId());
        newUser.setProfileImageUrl(kakaoUserInfo.getProfileImageUrl());
        newUser.setCreatedAt(LocalDate.now(ZoneOffset.UTC));
        newUser.setStatus(true);
        newUser.setRegistered(false);
        newUser.setDiaryCount(0L);
        newUser.setVisitedCountryCount(0L);
        newUser.setFlags("");
        User savedUser = userRepository.save(newUser);

        // JWT 토큰 발급 (AT + RT)
        String accessToken = jwtUtil.generateAccessToken(savedUser.getId(), savedUser.getKakaoId());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getId());

        return LoginResponseDto.from(savedUser, accessToken, refreshToken, false);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.isStatus()) {
            throw new CustomException(ErrorCode.ALREADY_LOGGED_OUT);
        }

        user.setStatus(false); // 로그아웃 시 status false로 설정
        // 프론트에서 accessToken, refreshToken 삭제 필요
    }
}

