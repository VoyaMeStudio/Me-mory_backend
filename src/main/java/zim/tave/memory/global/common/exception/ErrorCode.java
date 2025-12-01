package zim.tave.memory.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 파일 업로드
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 파일이 비어있습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "파일 크기가 허용 범위를 초과했습니다."),
    FILE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    //기본 설정
    INVALID_COUNTRY_CODE(HttpStatus.BAD_REQUEST, "올바르지 않은 국가 코드입니다."),
    INVALID_COUNTRY_EMOJI(HttpStatus.BAD_REQUEST, "올바르지 않은 국가 이모지입니다."),
    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다."),
    EMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, "감정을 찾을 수 없습니다."),
    //공통
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    //로그인, 회원가입
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "로그인 실패"),
    ALREADY_LOGGED_OUT(HttpStatus.BAD_REQUEST, "이미 로그아웃된 사용자입니다."),
    KAKAO_LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "카카오 로그인을 먼저 진행해주세요."),
    INCOMPLETE_USER_INFO(HttpStatus.BAD_REQUEST, "회원 정보 입력을 완료해주세요."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 가입된 사용자입니다."),
    MISSING_REQUIRED_FIELDS(HttpStatus.BAD_REQUEST, "값을 입력해주세요."),
    //설정
    //보관
    TRIP_NOT_STORED(HttpStatus.CONFLICT, "보관된 여행이 아닙니다."),
    DIARY_NOT_STORED(HttpStatus.CONFLICT, "보관된 일기가 아닙니다."),
    //여행
    DEFAULT_EMOTION_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "기본 감정 정보가 설정되어 있지 않습니다."),
    VISITED_COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "방문 국가 기록을 찾을 수 없습니다."),
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 여행을 찾을 수 없습니다."),
    TRIP_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 여행에 대한 수정 권한이 없습니다."),
    TRIP_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "여행명은 필수입니다."),
    TRIP_THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "여행 테마를 찾을 수 없습니다."),
    TRIP_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "여행명은 최대 14자입니다."),
    TRIP_DESCRIPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "여행 설명은 최대 56자입니다."),
    //일기
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다."),
    IMAGE_COUNT_INVALID(HttpStatus.BAD_REQUEST, "이미지는 반드시 2장이어야 합니다. (FRONT/BACK)"),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
    DIARY_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 일기에 대한 수정 권한이 없습니다."),
    CAMERA_TYPES_REQUIRED(HttpStatus.BAD_REQUEST, "FRONT/BACK 카메라 사진이 모두 필요합니다."),
    REPRESENTATIVE_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "대표 이미지는 정확히 1장이어야 합니다."),
    IMAGE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "이미지 ID가 필요합니다."),
    IMAGE_TRIP_MISMATCH(HttpStatus.BAD_REQUEST, "해당 이미지는 이 여행에 속하지 않습니다."),
    IMAGE_NOT_REPRESENTATIVE(HttpStatus.BAD_REQUEST, "대표 이미지가 아닙니다."),
    WEATHER_NOT_FOUND(HttpStatus.NOT_FOUND, "날씨를 찾을 수 없습니다."),
    CANNOT_ADD_DIARY_TO_PAST_TRIP(HttpStatus.BAD_REQUEST, "과거 여행에는 일기를 추가할 수 없습니다."),

    // 카카오로그인 관련 에러
    KAKAO_TOKEN_MISSING(HttpStatus.BAD_REQUEST, "카카오 액세스 토큰이 누락되었습니다."),
    KAKAO_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 카카오 액세스 토큰입니다."),
    KAKAO_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "카카오 API 접근 권한이 없습니다."),
    KAKAO_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 서버 오류입니다."),
    KAKAO_RESPONSE_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 응답 데이터를 처리할 수 없습니다."),
    KAKAO_API_UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 API 처리 중 알 수 없는 오류가 발생했습니다."),

    //보드
    BOARD_REQUIRED_FIELDS_MISSING(HttpStatus.BAD_REQUEST, "필수 입력값(boardThemeId, title)이 누락되었습니다."),
    BOARD_THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 보드 테마입니다."),
    BOARD_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "보드 생성 중 서버 오류가 발생했습니다."),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "보드를 찾을 수 없습니다."),
    BOARD_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 보드를 수정할 권한이 없습니다."),
    BOARD_STICKER_NOT_FOUND(HttpStatus.BAD_REQUEST, "스티커를 찾을 수 없습니다."),
    BOARD_UPDATE_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "보드 수정 중 서버 오류가 발생했습니다."),
    BOARD_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 보드를 삭제할 권한이 없습니다."),

    ;



    private final HttpStatus httpStatus;
    private final String message;
}

