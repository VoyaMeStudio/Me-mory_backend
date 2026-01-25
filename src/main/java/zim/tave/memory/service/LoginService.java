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

        KakaoUserInfo kakaoUserInfo = kakaoApiClient.getKakaoUserInfo(request.getAccessToken());
        User user = userRepository.findByKakaoId(kakaoUserInfo.getKakaoId()).orElse(null);


        if (user != null) {
            String token = jwtUtil.generateToken(user.getId(), user.getKakaoId());
            return LoginResponseDto.from(user, token, true);
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

        //JWT AT 발급
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getKakaoId());

        return LoginResponseDto.from(savedUser, token, false);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.isStatus()) {
            throw new CustomException(ErrorCode.ALREADY_LOGGED_OUT);
        }

        user.setStatus(false); //로그아웃 시 status false로 설정
        //프론트에서 accessToken 삭제
    }
}

