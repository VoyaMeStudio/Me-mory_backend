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
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_COUNTRY_CODE(HttpStatus.BAD_REQUEST, "올바르지 않은 국가 코드입니다."),
    INVALID_COUNTRY_EMOJI(HttpStatus.BAD_REQUEST, "올바르지 않은 국가 이모지입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    EMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, "감정을 찾을 수 없습니다."),
    DEFAULT_EMOTION_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "기본 감정 정보가 설정되어 있지 않습니다."),
    VISITED_COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "방문 국가 기록을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 여행을 찾을 수 없습니다."),
    TRIP_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 여행에 대한 수정 권한이 없습니다."),
    ALREADY_LOGGED_OUT(HttpStatus.BAD_REQUEST, "이미 로그아웃된 사용자입니다."),
    KAKAO_LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "카카오 로그인을 먼저 진행해주세요."),
    INCOMPLETE_USER_INFO(HttpStatus.BAD_REQUEST, "회원 정보 입력을 완료해주세요."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 가입된 사용자입니다.");



    private final HttpStatus httpStatus;
    private final String message;
}

