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
    LOGIN_SUCCESS(200, "로그인에 성공하였습니다."),
    JOIN_SUCCESS(200, "회원가입에 성공하였습니다."),

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
    TRIP_NOT_FOUND(404, "여행을 찾을 수 없습니다."),
    COUNTRY_NOT_FOUND(404, "국가를 찾을 수 없습니다."),
    EMOTION_NOT_FOUND(404, "감정을 찾을 수 없습니다."),
    DEFAULT_EMOTION_NOT_CONFIGURED(500, "기본 감정 정보가 설정되어 있지 않습니다."),
    VISITED_COUNTRY_NOT_FOUND(404, "방문 국가 기록을 찾을 수 없습니다."),
    SERVER_ERROR(500, "서버 오류가 발생했습니다."),
    ALREADY_LOGGED_OUT(400, "이미 로그아웃된 사용자입니다."),
    DIARY_NOT_FOUND(404, "일기를 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(404, "이미지를 찾을 수 없습니다."),
    IMAGE_COUNT_INVALID(400, "이미지는 반드시 2장이어야 합니다. (FRONT/BACK)"),
    CAMERA_TYPES_REQUIRED(400, "FRONT/BACK 카메라 사진이 모두 필요합니다."),
    REPRESENTATIVE_IMAGE_REQUIRED(400, "대표 이미지는 정확히 1장이어야 합니다."),
    IMAGE_ID_REQUIRED(400, "이미지 ID가 필요합니다."),
    IMAGE_TRIP_MISMATCH(400, "해당 이미지는 이 여행에 속하지 않습니다."),
    IMAGE_NOT_REPRESENTATIVE(400, "대표 이미지가 아닙니다."),
    TRIP_THEME_NOT_FOUND(404, "여행 테마를 찾을 수 없습니다."),
    WEATHER_NOT_FOUND(404, "날씨를 찾을 수 없습니다."),
    TRIP_NAME_REQUIRED(400, "여행명은 필수입니다."),
    TRIP_NAME_TOO_LONG(400, "여행명은 최대 14자입니다."),
    TRIP_DESCRIPTION_TOO_LONG(400, "여행 설명은 최대 56자입니다.");
    KAKAO_LOGIN_REQUIRED(400, "카카오 로그인을 먼저 진행해주세요."),
    INCOMPLETE_USER_INFO(400, "회원 정보 입력을 완료해주세요."),
    ALREADY_JOINED(409, "이미 가입된 사용자입니다.");

    private final int code;
    private final String message;
}
