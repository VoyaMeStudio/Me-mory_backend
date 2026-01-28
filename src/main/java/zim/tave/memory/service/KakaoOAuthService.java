package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.response.KakaoTokenResponse;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.kakao.KakaoApiClient;
import zim.tave.memory.kakao.KakaoUserInfo;
import zim.tave.memory.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    // 인가 코드로 카카오 액세스 토큰 요청
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    request,
                    KakaoTokenResponse.class
            );

            KakaoTokenResponse body = response.getBody();
            if (body == null || body.getAccessToken() == null) {
                throw new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
            }

            return body.getAccessToken();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        }
    }
}
