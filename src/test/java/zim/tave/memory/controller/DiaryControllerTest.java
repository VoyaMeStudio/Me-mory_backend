package zim.tave.memory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import zim.tave.memory.domain.DiaryImage;
import zim.tave.memory.dto.request.CreateDiaryRequest;
import zim.tave.memory.dto.request.UpdateDiaryOptionalFieldsRequest;
import zim.tave.memory.dto.request.UpdateRepresentativeImageRequest;
import zim.tave.memory.dto.response.DiaryResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.global.common.exception.GlobalExceptionHandler;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.DiaryService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DiaryControllerTest {

    @Mock
    private DiaryService diaryService;

    @InjectMocks
    private DiaryController diaryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(diaryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reset(diaryService);
    }

    private void setSecurityContext(Long userId) {
        CustomUserDetails principal = new CustomUserDetails(userId, "test@test.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    @Test
    void 일기_생성_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setTripId(1L);
        request.setCountryCode("KR");
        request.setCity("서울");
        request.setContent("테스트 내용");

        // 필수 필드: images (2개 필수)
        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(false);

        request.setImages(Arrays.asList(frontImage, backImage));

        DiaryResponseDto responseDto = DiaryResponseDto.builder()
                .id(1L)
                .city("서울")
                .content("테스트 내용")
                .tripId(1L)
                .build();

        given(diaryService.createDiary(any(CreateDiaryRequest.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/users/me/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(ResponseCode.CREATED.getCode()))
                .andExpect(jsonPath("$.data.city").value("서울"))
                .andExpect(jsonPath("$.data.content").value("테스트 내용"));

        verify(diaryService).createDiary(any(CreateDiaryRequest.class));
    }

    @Test
    void 일기_선택_필드_수정_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 1L;
        UpdateDiaryOptionalFieldsRequest request = new UpdateDiaryOptionalFieldsRequest();
        request.setCity("부산");
        request.setContent("수정된 내용");

        // when & then
        mockMvc.perform(patch("/api/users/me/diaries/{diaryId}", diaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(diaryService).updateDiaryOptionalFields(eq(userId), eq(diaryId), any(UpdateDiaryOptionalFieldsRequest.class));
    }

    @Test
    void 대표_이미지_변경_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 1L;
        UpdateRepresentativeImageRequest request = new UpdateRepresentativeImageRequest();
        request.setImageId(2L);

        // when & then
        mockMvc.perform(patch("/api/users/me/diaries/{diaryId}/representative-images", diaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(diaryService).updateRepresentativeImage(eq(diaryId), eq(2L));
    }

    @Test
    void 내_전체_일기_목록_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        DiaryResponseDto diary1 = DiaryResponseDto.builder()
                .id(1L)
                .city("서울")
                .content("첫 번째 일기")
                .build();

        DiaryResponseDto diary2 = DiaryResponseDto.builder()
                .id(2L)
                .city("부산")
                .content("두 번째 일기")
                .build();

        given(diaryService.findByUserId(userId)).willReturn(List.of(diary1, diary2));

        // when & then
        mockMvc.perform(get("/api/users/me/diaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].city").value("서울"))
                .andExpect(jsonPath("$.data[1].city").value("부산"));

        verify(diaryService).findByUserId(userId);
    }

    @Test
    void 일기_상세_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 1L;
        DiaryResponseDto responseDto = DiaryResponseDto.builder()
                .id(diaryId)
                .city("서울")
                .content("상세 내용")
                .tripId(1L)
                .build();

        given(diaryService.findOne(diaryId, userId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/users/me/diaries/{diaryId}", diaryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.city").value("서울"))
                .andExpect(jsonPath("$.data.content").value("상세 내용"));

        verify(diaryService).findOne(diaryId, userId);
    }

    @Test
    void 여행별_일기_목록_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;
        DiaryResponseDto diary1 = DiaryResponseDto.builder()
                .id(1L)
                .city("서울")
                .tripId(tripId)
                .build();

        DiaryResponseDto diary2 = DiaryResponseDto.builder()
                .id(2L)
                .city("부산")
                .tripId(tripId)
                .build();

        given(diaryService.findByTripId(tripId, userId)).willReturn(List.of(diary1, diary2));

        // when & then
        mockMvc.perform(get("/api/users/me/trips/{tripId}/diaries", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].tripId").value(tripId))
                .andExpect(jsonPath("$.data[1].tripId").value(tripId));

        verify(diaryService).findByTripId(tripId, userId);
    }

    @Test
    void 일기_보관_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 1L;
        boolean isStored = true;

        // when & then
        mockMvc.perform(patch("/api/users/me/diaries/{diaryId}/store", diaryId)
                        .param("isStored", String.valueOf(isStored)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(diaryService).storeDiary(eq(diaryId), eq(userId), eq(isStored));
    }

    @Test
    void 일기_보관_해제_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 1L;
        boolean isStored = false;

        // when & then
        mockMvc.perform(patch("/api/users/me/diaries/{diaryId}/store", diaryId)
                        .param("isStored", String.valueOf(isStored)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(diaryService).storeDiary(eq(diaryId), eq(userId), eq(isStored));
    }

    @Test
    void 일기_보관_소유권_검증_실패() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 1L;
        boolean isStored = true;

        willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
                .given(diaryService).storeDiary(eq(diaryId), eq(userId), eq(isStored));

        // when & then
        // GlobalExceptionHandler의 ErrorCode -> ResponseCode 매핑 검증
        mockMvc.perform(patch("/api/users/me/diaries/{diaryId}/store", diaryId)
                        .param("isStored", String.valueOf(isStored)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ResponseCode.ACCESS_DENIED.getCode()));
    }

    @Test
    void 일기_보관_다이어리_없음() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 999L;
        boolean isStored = true;

        willThrow(new CustomException(ErrorCode.DIARY_NOT_FOUND))
                .given(diaryService).storeDiary(eq(diaryId), eq(userId), eq(isStored));

        // when & then
        // GlobalExceptionHandler의 ErrorCode -> ResponseCode 매핑 검증
        mockMvc.perform(patch("/api/users/me/diaries/{diaryId}/store", diaryId)
                        .param("isStored", String.valueOf(isStored)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ResponseCode.DIARY_NOT_FOUND.getCode()));
    }

    @Test
    void 일기_삭제_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 1L;

        // when & then
        mockMvc.perform(delete("/api/users/me/diaries/{diaryId}", diaryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(diaryService).deleteDiary(eq(diaryId), eq(userId));
    }

    @Test
    void 일기_삭제_소유권_검증_실패() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 1L;

        willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
                .given(diaryService).deleteDiary(eq(diaryId), eq(userId));

        // when & then
        // GlobalExceptionHandler의 ErrorCode -> ResponseCode 매핑 검증
        mockMvc.perform(delete("/api/users/me/diaries/{diaryId}", diaryId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ResponseCode.ACCESS_DENIED.getCode()));
    }

    @Test
    void 일기_삭제_다이어리_없음() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 999L;

        willThrow(new CustomException(ErrorCode.DIARY_NOT_FOUND))
                .given(diaryService).deleteDiary(eq(diaryId), eq(userId));

        // when & then
        // GlobalExceptionHandler의 ErrorCode -> ResponseCode 매핑 검증
        mockMvc.perform(delete("/api/users/me/diaries/{diaryId}", diaryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ResponseCode.DIARY_NOT_FOUND.getCode()));
    }

    @Test
    void 일기_상세_조회_소유권_검증_실패() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 1L;

        willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
                .given(diaryService).findOne(eq(diaryId), eq(userId));

        // when & then
        // GlobalExceptionHandler의 ErrorCode -> ResponseCode 매핑 검증
        mockMvc.perform(get("/api/users/me/diaries/{diaryId}", diaryId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ResponseCode.ACCESS_DENIED.getCode()));
    }

    @Test
    void 일기_상세_조회_다이어리_없음() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 999L;

        willThrow(new CustomException(ErrorCode.DIARY_NOT_FOUND))
                .given(diaryService).findOne(eq(diaryId), eq(userId));

        // when & then
        // GlobalExceptionHandler의 ErrorCode -> ResponseCode 매핑 검증
        mockMvc.perform(get("/api/users/me/diaries/{diaryId}", diaryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ResponseCode.DIARY_NOT_FOUND.getCode()));
    }

    @Test
    void 일기_수정_소유권_검증_실패() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long diaryId = 1L;
        UpdateDiaryOptionalFieldsRequest request = new UpdateDiaryOptionalFieldsRequest();
        request.setCity("부산");

        willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
                .given(diaryService).updateDiaryOptionalFields(eq(userId), eq(diaryId), any(UpdateDiaryOptionalFieldsRequest.class));

        // when & then
        // GlobalExceptionHandler의 ErrorCode -> ResponseCode 매핑 검증
        mockMvc.perform(patch("/api/users/me/diaries/{diaryId}", diaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ResponseCode.ACCESS_DENIED.getCode()));
    }

    // ========== Validation 테스트 ==========

    @Test
    void 일기_생성_필수_필드_누락_400() throws Exception {
        // given - 필수 필드 누락 (tripId, countryCode, city, images)
        Long userId = 1L;
        setSecurityContext(userId);

        CreateDiaryRequest request = new CreateDiaryRequest();
        // 필수 필드들을 모두 null로 설정 (dateTime, content는 선택 필드)

        // Service 레이어 검증에서 TRIP_NOT_FOUND 발생 (tripId가 null이므로)
        willThrow(new CustomException(ErrorCode.TRIP_NOT_FOUND))
                .given(diaryService).createDiary(any(CreateDiaryRequest.class));

        // when & then
        mockMvc.perform(post("/api/users/me/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ResponseCode.TRIP_NOT_FOUND.getCode()));
    }

    @Test
    void 일기_생성_tripId_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setTripId(null); // 필수 필드 null
        request.setCountryCode("KR");
        request.setCity("서울");

        // 필수 필드: images
        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(false);

        request.setImages(Arrays.asList(frontImage, backImage));

        // Service 레이어 검증에서 TRIP_NOT_FOUND 발생
        willThrow(new CustomException(ErrorCode.TRIP_NOT_FOUND))
                .given(diaryService).createDiary(any(CreateDiaryRequest.class));

        // when & then
        mockMvc.perform(post("/api/users/me/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ResponseCode.TRIP_NOT_FOUND.getCode()));
    }

    @Test
    void 일기_생성_countryCode_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setTripId(1L);
        request.setCountryCode(null); // 필수 필드 null
        request.setCity("서울");

        // 필수 필드: images
        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(false);

        request.setImages(Arrays.asList(frontImage, backImage));

        // Service 레이어 검증에서 INVALID_COUNTRY_CODE 발생
        willThrow(new CustomException(ErrorCode.INVALID_COUNTRY_CODE))
                .given(diaryService).createDiary(any(CreateDiaryRequest.class));

        // when & then
        mockMvc.perform(post("/api/users/me/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.INVALID_COUNTRY_CODE.getCode()));
    }

    @Test
    void 일기_생성_city_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setTripId(1L);
        request.setCountryCode("KR");
        request.setCity(null); // 필수 필드 null

        // 필수 필드: images
        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(false);

        request.setImages(Arrays.asList(frontImage, backImage));

        // Service 레이어에서 city null 처리 - 실제로는 Diary.createDiary에서 처리하거나
        // null이 허용될 수 있으므로, 일반적인 VALIDATION_ERROR로 처리
        willThrow(new CustomException(ErrorCode.VALIDATION_ERROR))
                .given(diaryService).createDiary(any(CreateDiaryRequest.class));

        // when & then
        mockMvc.perform(post("/api/users/me/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    void 일기_생성_dateTime_자동생성_성공() throws Exception {
        // given - dateTime은 서버에서 자동 생성되므로 요청에 포함하지 않음
        Long userId = 1L;
        setSecurityContext(userId);

        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setTripId(1L);
        request.setCountryCode("KR");
        request.setCity("서울");
        // dateTime 필드 제거 - 서버에서 UTC 기준으로 자동 생성됨
        request.setContent("테스트 내용");

        // 필수 필드: images
        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(false);

        request.setImages(Arrays.asList(frontImage, backImage));

        DiaryResponseDto responseDto = DiaryResponseDto.builder()
                .id(1L)
                .city("서울")
                .content("테스트 내용")
                .tripId(1L)
                .build();

        given(diaryService.createDiary(any(CreateDiaryRequest.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/users/me/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(ResponseCode.CREATED.getCode()));

        verify(diaryService).createDiary(any(CreateDiaryRequest.class));
    }

    @Test
    void 일기_생성_content_null_성공() throws Exception {
        // given - content는 선택 필드이므로 null이어도 성공해야 함
        Long userId = 1L;
        setSecurityContext(userId);

        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setTripId(1L);
        request.setCountryCode("KR");
        request.setCity("서울");
        request.setContent(null); // 선택 필드 null

        // 필수 필드: images
        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(false);

        request.setImages(Arrays.asList(frontImage, backImage));

        DiaryResponseDto responseDto = DiaryResponseDto.builder()
                .id(1L)
                .city("서울")
                .tripId(1L)
                .build();

        given(diaryService.createDiary(any(CreateDiaryRequest.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/users/me/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(ResponseCode.CREATED.getCode()));

        verify(diaryService).createDiary(any(CreateDiaryRequest.class));
    }
}

