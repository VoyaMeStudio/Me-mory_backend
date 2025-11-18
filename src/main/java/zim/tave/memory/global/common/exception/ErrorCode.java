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
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
    IMAGE_COUNT_INVALID(HttpStatus.BAD_REQUEST, "이미지는 반드시 2장이어야 합니다. (FRONT/BACK)"),
    CAMERA_TYPES_REQUIRED(HttpStatus.BAD_REQUEST, "FRONT/BACK 카메라 사진이 모두 필요합니다."),
    REPRESENTATIVE_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "대표 이미지는 정확히 1장이어야 합니다."),
    IMAGE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "이미지 ID가 필요합니다."),
    IMAGE_TRIP_MISMATCH(HttpStatus.BAD_REQUEST, "해당 이미지는 이 여행에 속하지 않습니다."),
    IMAGE_NOT_REPRESENTATIVE(HttpStatus.BAD_REQUEST, "대표 이미지가 아닙니다."),
    TRIP_THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "여행 테마를 찾을 수 없습니다."),
    WEATHER_NOT_FOUND(HttpStatus.NOT_FOUND, "날씨를 찾을 수 없습니다."),
    TRIP_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "여행명은 필수입니다."),
    TRIP_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "여행명은 최대 14자입니다."),
    TRIP_DESCRIPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "여행 설명은 최대 56자입니다."),
    KAKAO_LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "카카오 로그인을 먼저 진행해주세요."),
    INCOMPLETE_USER_INFO(HttpStatus.BAD_REQUEST, "회원 정보 입력을 완료해주세요."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 가입된 사용자입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    NO_FIELDS_TO_UPDATE(HttpStatus.BAD_REQUEST, "수정할 필드가 존재하지 않습니다."),
    MISSING_REQUIRED_FIELDS(HttpStatus.BAD_REQUEST, "값을 입력해주세요."),
    STORED_TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "보관된 여행 정보를 찾을 수 없습니다.");



    private final HttpStatus httpStatus;
    private final String message;
}

