package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.response.KakaoTokenResponse;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.kakao.KakaoApiClient;
import zim.tave.memory.kakao.KakaoUserInfo;
import zim.tave.memory.repository.UserRepository;

import java.nio.charset.StandardCharsets;

@Slf4j
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

        log.error("[KAKAO SERVICE] start. code={}, redirectUri={}, clientIdPrefix={}",
                code,
                redirectUri,
                clientId != null ? clientId.substring(0, 6) : "null");

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

            log.info("[KAKAO][TOKEN] response status={}, hasBody={}",
                    response.getStatusCode(), response.hasBody());

            KakaoTokenResponse body = response.getBody();
            if (body == null || body.getAccessToken() == null) {
                throw new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
            }

            if (body.getAccessToken() == null || body.getAccessToken().isBlank()) {
                log.error("[KAKAO][TOKEN] accessToken missing in body. check parsing/response. status={}",
                        response.getStatusCode());
                throw new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
            }

            log.info("[KAKAO][TOKEN] success. accessTokenLen={}", body.getAccessToken().length());
            return body.getAccessToken();


        } catch (HttpStatusCodeException e) {
            // 여기서가 핵심: 카카오가 준 에러 본문을 남겨야 원인(redirect mismatch / invalid_client 등) 확정 가능
            String respBody = e.getResponseBodyAsString(StandardCharsets.UTF_8);

            log.error("[KAKAO][TOKEN] HTTP error. status={}, responseBody={}, redirectUri={}, clientIdPrefix={}, code={}",
                    e.getStatusCode(),
                    shrink(respBody),
                    redirectUri,
                    prefix(clientId),
                    code
            );

            throw new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);

        } catch (ResourceAccessException e) {
            // 네트워크/타임아웃
            log.error("[KAKAO][TOKEN] network error. msg={}, redirectUri={}, code={}",
                    e.getMessage(), redirectUri, code, e);
            throw new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);

        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            log.error("[KAKAO][TOKEN] unknown error. redirectUri={}, code={}",
                    redirectUri, code, e);
            throw new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        }
    }

    private String prefix(String s) {
        if (s == null) return "null";
        return s.length() <= 6 ? s : s.substring(0, 6) + "...";
    }

    private String shrink(String s) {
        if (s == null) return "null";
        // 로그 폭주 방지
        return s.length() <= 500 ? s : s.substring(0, 500) + "...(truncated)";
    }
}
