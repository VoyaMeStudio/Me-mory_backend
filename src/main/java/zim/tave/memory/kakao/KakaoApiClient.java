package zim.tave.memory.kakao;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;

//accessToken으로 카카오 정보 가져옴
@Component
public class KakaoApiClient {
    private final RestTemplate restTemplate = new RestTemplate();

    public KakaoUserInfo getKakaoUserInfo(String accessToken){

        if (accessToken == null || accessToken.isBlank()) {
            throw new CustomException(ErrorCode.KAKAO_TOKEN_MISSING);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoInfoResponse> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    entity,
                    KakaoInfoResponse.class
            );

            // HTTP Status 체크
            int status = response.getStatusCodeValue();

            if (status == 401) { // Unauthorized
                throw new CustomException(ErrorCode.KAKAO_INVALID_TOKEN);
            }
            if (status == 403) { // Forbidden
                throw new CustomException(ErrorCode.KAKAO_UNAUTHORIZED);
            }
            if (status >= 500) {
                throw new CustomException(ErrorCode.KAKAO_SERVER_ERROR);
            }

            KakaoInfoResponse body = response.getBody();
            if (body == null || body.getId() == null || body.getKakaoAccount() == null) {
                throw new CustomException(ErrorCode.KAKAO_RESPONSE_PARSING_ERROR);
            }

            return new KakaoUserInfo(
                    String.valueOf(body.getId()),
                    body.getKakaoAccount().getProfile().getProfileImageUrl()
            );

        } catch (CustomException e) {
            throw e; // 정확한 에러코드 그대로 전달
        } catch (Exception e) {
            throw new CustomException(ErrorCode.KAKAO_API_UNKNOWN_ERROR);
        }
    }
}
