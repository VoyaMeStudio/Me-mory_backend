package zim.tave.memory.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    // 성공 응답
    SUCCESS(200, "성공하였습니다."),
    CREATED(201, "생성되었습니다."),
    LOGOUT_SUCCESS(200, "로그아웃 성공"),

    // 실패 응답
    INVALID_REQUEST(400, "잘못된 요청입니다."),
    VALIDATION_ERROR(400, "요청 값이 올바르지 않습니다."),
    INVALID_COUNTRY_CODE(400, "올바르지 않은 국가 코드입니다."),
    INVALID_COUNTRY_EMOJI(400, "올바르지 않은 국가 이모지입니다."),
    AUTHENTICATION_FAILED(401, "인증에 실패했습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    ACCESS_DENIED(403, "접근 권한이 없습니다."),
    LOGIN_FAIL(401, "로그인 실패"),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    COUNTRY_NOT_FOUND(404, "국가를 찾을 수 없습니다."),
    EMOTION_NOT_FOUND(404, "감정을 찾을 수 없습니다."),
    DEFAULT_EMOTION_NOT_CONFIGURED(500, "기본 감정 정보가 설정되어 있지 않습니다."),
    VISITED_COUNTRY_NOT_FOUND(404, "방문 국가 기록을 찾을 수 없습니다."),
    SERVER_ERROR(500, "서버 오류가 발생했습니다.");

    private final int code;
    private final String message;
}
