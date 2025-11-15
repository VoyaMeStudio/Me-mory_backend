package zim.tave.memory.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "로그인 실패"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    ALREADY_LOGGED_OUT(HttpStatus.BAD_REQUEST, "이미 로그아웃된 사용자입니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 가입된 사용자입니다."),
    KAKAO_LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "카카오 로그인을 먼저 진행해주세요.");

    private final HttpStatus httpStatus;
    private final String message;
}

