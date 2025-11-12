package zim.tave.memory.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    // 성공 응답
    SUCCESS(200, "성공하였습니다."),
    LOGOUT_SUCCESS(200, "로그아웃 성공"),

    // 실패 응답
    LOGIN_FAIL(401, "로그인 실패"),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    SERVER_ERROR(500, "서버 오류가 발생했습니다.");

    private final int code;
    private final String message;
}
