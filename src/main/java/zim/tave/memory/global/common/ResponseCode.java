package zim.tave.memory.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    // 성공 응답

    //공통
    SUCCESS(200, "성공하였습니다."),
    CREATED(201, "생성되었습니다."),
    //로그인
    LOGOUT_SUCCESS(200, "로그아웃 성공"),
    LOGIN_SUCCESS(200, "로그인에 성공하였습니다."),
    //회원가입
    JOIN_SUCCESS(200, "회원가입에 성공하였습니다."),
    //마이페이지
    VISITED_COUNTRIES_FETCH_SUCCESS(200, "방문한 국가 목록을 성공적으로 조회하였습니다."),
    USER_UPDATE_SUCCESS(200, "회원 정보가 성공적으로 수정되었습니다."),
    //보관
    STORED_TRIP_FOUND_SUCCESS(200, "보관된 여행 목록을 성공적으로 조회하였습니다."),
    STORED_DIARY_FOUND_SUCCESS(200, "보관된 일기 목록을 성공적으로 조회하였습니다."),
    //설정
    USER_DELETE_SUCCESS(200, "회원 탈퇴가 성공적으로 처리되었습니다."),
    NO_FIELDS_TO_UPDATE(400, "수정할 필드가 없습니다."),
    //카카오 로그인 성공 메세지
    KAKAO_USERINFO_FETCH_SUCCESS(200, "카카오 사용자 정보 조회 성공"),
    TOKEN_REFRESH_SUCCESS(200, "토큰 갱신에 성공했습니다."),
    //보드
    BOARD_CREATE_SUCCESS(200, "보드 생성에 성공하였습니다."),
    BOARD_UPDATE_SUCCESS(200, "보드 수정이 완료되었습니다."),
    BOARD_DELETE_SUCCESS(200, "보드가 성공적으로 삭제되었습니다."),
    BOARD_FETCH_SUCCESS(200, "보드 정보가 성공적으로 조회되었습니다."),
    //알림
    NO_ALARM_TO_SEND(200, "전송할 알림이 없습니다."),

    // 실패 응답
    INVALID_REQUEST(400, "잘못된 요청입니다."),
    VALIDATION_ERROR(400, "요청 값이 올바르지 않습니다."),
    AUTHENTICATION_FAILED(401, "인증에 실패했습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    ACCESS_DENIED(403, "접근 권한이 없습니다."),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    SERVER_ERROR(500, "서버 오류가 발생했습니다."),
    UNAUTHORIZED_USER(401, "인증되지 않은 사용자입니다."),

    INVALID_COUNTRY_CODE(400, "올바르지 않은 국가 코드입니다."),
    INVALID_COUNTRY_EMOJI(400, "올바르지 않은 국가 이모지입니다."),
    //파일업로드
    FILE_EMPTY(400, "업로드할 파일이 비어있습니다."),
    FILE_TOO_LARGE(400, "파일 크기가 허용 범위를 초과했습니다."),
    FILE_TYPE_NOT_ALLOWED(400, "지원하지 않는 파일 형식입니다."),
    FILE_UPLOAD_FAILED(500, "파일 업로드에 실패했습니다."),
    //로그인
    LOGIN_FAIL(401, "로그인 실패"),
    ALREADY_LOGGED_OUT(400, "이미 로그아웃된 사용자입니다."),
    //회원가입
    KAKAO_LOGIN_REQUIRED(400, "카카오 로그인을 먼저 진행해주세요."),
    INCOMPLETE_USER_INFO(400, "회원 정보 입력을 완료해주세요."),
    ALREADY_JOINED(409, "이미 가입된 사용자입니다."),
    //설정, 마이페이지
    MISSING_REQUIRED_FIELDS(400, "값을 입력해주세요."),
    STORED_TRIP_NOT_FOUND(404, "보관된 여행 정보를 찾을 수 없습니다."),
    //보관
    TRIP_NOT_STORED(409, "보관된 여행이 아닙니다."),
    DIARY_NOT_STORED(409, "보관된 일기가 아닙니다."),

    //국가
    COUNTRY_NOT_FOUND(404, "국가를 찾을 수 없습니다."),
    VISITED_COUNTRY_NOT_FOUND(404, "방문 국가 기록을 찾을 수 없습니다."),
    //감정
    EMOTION_NOT_FOUND(404, "감정을 찾을 수 없습니다."),
    DEFAULT_EMOTION_NOT_CONFIGURED(500, "기본 감정 정보가 설정되어 있지 않습니다."),
    DIARY_NOT_FOUND(404, "일기를 찾을 수 없습니다."),
    //일기
    IMAGE_NOT_FOUND(404, "이미지를 찾을 수 없습니다."),
    IMAGE_COUNT_INVALID(400, "이미지는 반드시 2장이어야 합니다. (FRONT/BACK)"),
    CAMERA_TYPES_REQUIRED(400, "FRONT/BACK 카메라 사진이 모두 필요합니다."),
    REPRESENTATIVE_IMAGE_REQUIRED(400, "대표 이미지는 정확히 1장이어야 합니다."),
    IMAGE_ID_REQUIRED(400, "이미지 ID가 필요합니다."),
    //여행
    TRIP_NOT_FOUND(404, "여행을 찾을 수 없습니다."),
    IMAGE_NOT_REPRESENTATIVE(400, "대표 이미지가 아닙니다."),
    IMAGE_TRIP_MISMATCH(400, "해당 이미지는 이 여행에 속하지 않습니다."),
    TRIP_THEME_NOT_FOUND(404, "여행 테마를 찾을 수 없습니다."),
    WEATHER_NOT_FOUND(404, "날씨를 찾을 수 없습니다."),
    TRIP_NAME_REQUIRED(400, "여행명은 필수입니다."),
    TRIP_NAME_TOO_LONG(400, "여행명은 최대 14자입니다."),
    TRIP_DESCRIPTION_TOO_LONG(400, "여행 설명은 최대 56자입니다."),
    NOT_PAST_TRIP(400, "과거 여행만 수정/보관/삭제할 수 있습니다."),
    CANNOT_ADD_DIARY_TO_PAST_TRIP(400, "과거 여행에는 일기를 추가할 수 없습니다."),

    // JWT 토큰 관련 세분화된 에러
    EXPIRED_TOKEN(401, "토큰이 만료되었습니다."),
    MALFORMED_TOKEN(401, "올바르지 않은 JWT 형식입니다."),
    INVALID_TOKEN_SIGNATURE(401, "JWT 서명이 유효하지 않습니다."),
    UNSUPPORTED_TOKEN(401, "지원하지 않는 JWT 토큰입니다."),


    // 카카오로그인 관련 에러
    KAKAO_TOKEN_MISSING(400, "카카오 액세스 토큰이 누락되었습니다."),
    KAKAO_INVALID_TOKEN(401, "유효하지 않거나 만료된 카카오 액세스 토큰입니다."),
    KAKAO_UNAUTHORIZED(401, "카카오 API 접근 권한이 없습니다."),
    KAKAO_SERVER_ERROR(500, "카카오 서버 오류입니다."),
    KAKAO_RESPONSE_PARSING_ERROR(500, "카카오 응답 데이터를 처리할 수 없습니다."),
    KAKAO_API_UNKNOWN_ERROR(500, "카카오 API 처리 중 알 수 없는 오류가 발생했습니다."),
    KAKAO_TOKEN_REQUEST_FAILED(400, "카카오 토큰 요청에 실패했습니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 Refresh Token입니다."),

    //보드
    BOARD_REQUIRED_FIELDS_MISSING(400, "필수 입력값(boardThemeId, title)이 누락되었습니다."),
    BOARD_THEME_NOT_FOUND(404, "존재하지 않는 보드 테마입니다."),
    BOARD_CREATE_FAILED(500, "보드 생성 중 서버 오류가 발생했습니다."),
    BOARD_NOT_FOUND(404, "보드를 찾을 수 없습니다."),
    BOARD_UPDATE_FORBIDDEN(403, "해당 보드를 수정할 권한이 없습니다."),
    BOARD_STICKER_NOT_FOUND(404, "스티커를 찾을 수 없습니다."),
    BOARD_UPDATE_INTERNAL_ERROR(500, "보드 수정 중 서버 오류가 발생했습니다."),
    BOARD_DELETE_FORBIDDEN(403, "해당 보드를 삭제할 권한이 없습니다."),
    BOARD_STICKER_MAP_NOT_FOUND(404, "스티커가 보드에 존재하지 않습니다."),
    BOARD_ACCESS_FORBIDDEN(403, "해당 보드에 접근할 수 없습니다."),

    //알림
    ALARM_NOT_AGREED(403, "알림 수신 미동의 사용자입니다."),
    ;

    private final int code;
    private final String message;
}
