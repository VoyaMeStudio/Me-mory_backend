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
import zim.tave.memory.dto.TimelineTripDto;
import zim.tave.memory.dto.request.CreatePastTripRequest;
import zim.tave.memory.dto.request.UpdateTripRequest;
import zim.tave.memory.dto.response.TimelineResponseDto;
import zim.tave.memory.dto.response.TripResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.global.common.exception.GlobalExceptionHandler;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.TimelineService;
import zim.tave.memory.service.TripService;

import java.time.LocalDate;
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
class TimelineControllerTest {

    @Mock
    private TimelineService timelineService;

    @Mock
    private TripService tripService;

    @InjectMocks
    private TimelineController timelineController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(timelineController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reset(timelineService, tripService);
    }

    private void setSecurityContext(Long userId) {
        CustomUserDetails principal = new CustomUserDetails(userId, "test@test.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    @Test
    void 타임라인_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        TimelineTripDto tripDto = TimelineTripDto.builder()
                .tripId(1L)
                .tripName("제주도 여행")
                .description("제주도 3박 4일 여행")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 5))
                .isPast(false)
                .emotionName("설렘")
                .emotionColor("#FF6B6B")
                .representativeImageUrl("https://example.com/image.jpg")
                .visitedCountries(List.of())
                .build();

        TimelineResponseDto responseDto = TimelineResponseDto.builder()
                .trips(List.of(tripDto))
                .build();

        given(timelineService.getTimeline(userId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/users/me/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.trips[0].tripId").value(1L))
                .andExpect(jsonPath("$.data.trips[0].tripName").value("제주도 여행"))
                .andExpect(jsonPath("$.data.trips[0].emotionName").value("설렘"))
                .andExpect(jsonPath("$.data.trips[0].emotionColor").value("#FF6B6B"));

        verify(timelineService).getTimeline(userId);
    }

    @Test
    void 과거_여행_생성_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreatePastTripRequest request = new CreatePastTripRequest();
        request.setTripName("유럽 배낭여행");
        request.setDescription("2주간 여행");
        request.setStartDate(LocalDate.of(2023, 5, 1));
        request.setEndDate(LocalDate.of(2023, 5, 14));
        request.setCountryCodes(List.of("FR", "IT"));
        request.setEmotionId(2L);

        TripResponseDto responseDto = TripResponseDto.builder()
                .id(1L)
                .tripName("유럽 배낭여행")
                .description("2주간 여행")
                .startDate(LocalDate.of(2023, 5, 1))
                .endDate(LocalDate.of(2023, 5, 14))
                .build();

        given(tripService.createPastTrip(any(CreatePastTripRequest.class), eq(userId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/users/me/timeline/past")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(ResponseCode.CREATED.getCode()))
                .andExpect(jsonPath("$.data.tripName").value("유럽 배낭여행"));

        verify(tripService).createPastTrip(any(CreatePastTripRequest.class), eq(userId));
    }

    @Test
    void 과거_여행_수정_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 여행명");
        request.setDescription("수정된 설명");

        // when & then
        mockMvc.perform(patch("/api/users/me/timeline/past/{tripId}", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(tripService).updatePastTrip(eq(tripId), any(UpdateTripRequest.class), eq(userId));
    }

    @Test
    void 과거_여행_수정_실패_과거_여행_아님() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 여행명");

        willThrow(new CustomException(ErrorCode.NOT_PAST_TRIP))
                .given(tripService).updatePastTrip(eq(tripId), any(UpdateTripRequest.class), eq(userId));

        // when & then
        mockMvc.perform(patch("/api/users/me/timeline/past/{tripId}", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.NOT_PAST_TRIP.getCode()));
    }

    @Test
    void 과거_여행_보관_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;
        boolean isStored = true;

        // when & then
        mockMvc.perform(patch("/api/users/me/timeline/past/{tripId}/store", tripId)
                        .param("isStored", String.valueOf(isStored)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(tripService).storePastTrip(eq(tripId), eq(userId), eq(isStored));
    }

    @Test
    void 과거_여행_보관_실패_과거_여행_아님() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;
        boolean isStored = true;

        willThrow(new CustomException(ErrorCode.NOT_PAST_TRIP))
                .given(tripService).storePastTrip(eq(tripId), eq(userId), eq(isStored));

        // when & then
        mockMvc.perform(patch("/api/users/me/timeline/past/{tripId}/store", tripId)
                        .param("isStored", String.valueOf(isStored)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.NOT_PAST_TRIP.getCode()));
    }

    @Test
    void 과거_여행_삭제_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;

        // when & then
        mockMvc.perform(delete("/api/users/me/timeline/past/{tripId}", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(tripService).deletePastTrip(eq(tripId), eq(userId));
    }

    @Test
    void 과거_여행_삭제_실패_과거_여행_아님() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;

        willThrow(new CustomException(ErrorCode.NOT_PAST_TRIP))
                .given(tripService).deletePastTrip(eq(tripId), eq(userId));

        // when & then
        mockMvc.perform(delete("/api/users/me/timeline/past/{tripId}", tripId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.NOT_PAST_TRIP.getCode()));
    }

    @Test
    void 과거_여행_수정_소유권_검증_실패() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 여행명");

        willThrow(new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN))
                .given(tripService).updatePastTrip(eq(tripId), any(UpdateTripRequest.class), eq(userId));

        // when & then
        mockMvc.perform(patch("/api/users/me/timeline/past/{tripId}", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ResponseCode.ACCESS_DENIED.getCode()));
    }

    @Test
    void 과거_여행_수정_여행_없음() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 999L;
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 여행명");

        willThrow(new CustomException(ErrorCode.TRIP_NOT_FOUND))
                .given(tripService).updatePastTrip(eq(tripId), any(UpdateTripRequest.class), eq(userId));

        // when & then
        mockMvc.perform(patch("/api/users/me/timeline/past/{tripId}", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ResponseCode.TRIP_NOT_FOUND.getCode()));
    }

    // ========== Validation 테스트 ==========

    @Test
    void 과거_여행_생성_tripName_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreatePastTripRequest request = new CreatePastTripRequest();
        request.setTripName(null); // 필수 필드 null
        request.setStartDate(LocalDate.of(2023, 5, 1));
        request.setEndDate(LocalDate.of(2023, 5, 14));
        request.setCountryCodes(List.of("FR", "IT"));
        request.setEmotionId(2L);

        // Service 레이어 검증에서 TRIP_NAME_REQUIRED 발생
        willThrow(new CustomException(ErrorCode.TRIP_NAME_REQUIRED))
                .given(tripService).createPastTrip(any(CreatePastTripRequest.class), eq(userId));

        // when & then
        mockMvc.perform(post("/api/users/me/timeline/past")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.TRIP_NAME_REQUIRED.getCode()));
    }

    @Test
    void 과거_여행_생성_tripName_길이초과_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreatePastTripRequest request = new CreatePastTripRequest();
        request.setTripName("매우 긴 여행 이름입니다 이것은 14자를 초과합니다"); // 길이 초과
        request.setStartDate(LocalDate.of(2023, 5, 1));
        request.setEndDate(LocalDate.of(2023, 5, 14));
        request.setCountryCodes(List.of("FR", "IT"));
        request.setEmotionId(2L);

        // Service 레이어 검증에서 TRIP_NAME_TOO_LONG 발생
        willThrow(new CustomException(ErrorCode.TRIP_NAME_TOO_LONG))
                .given(tripService).createPastTrip(any(CreatePastTripRequest.class), eq(userId));

        // when & then
        mockMvc.perform(post("/api/users/me/timeline/past")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.TRIP_NAME_TOO_LONG.getCode()));
    }

    @Test
    void 과거_여행_생성_startDate_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreatePastTripRequest request = new CreatePastTripRequest();
        request.setTripName("유럽 배낭여행");
        request.setStartDate(null); // 필수 필드 null
        request.setEndDate(LocalDate.of(2023, 5, 14));
        request.setCountryCodes(List.of("FR", "IT"));
        request.setEmotionId(2L);

        // Service 레이어 검증에서 MISSING_REQUIRED_FIELDS 발생
        willThrow(new CustomException(ErrorCode.MISSING_REQUIRED_FIELDS))
                .given(tripService).createPastTrip(any(CreatePastTripRequest.class), eq(userId));

        // when & then
        mockMvc.perform(post("/api/users/me/timeline/past")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void 과거_여행_생성_endDate_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreatePastTripRequest request = new CreatePastTripRequest();
        request.setTripName("유럽 배낭여행");
        request.setStartDate(LocalDate.of(2023, 5, 1));
        request.setEndDate(null); // 필수 필드 null
        request.setCountryCodes(List.of("FR", "IT"));
        request.setEmotionId(2L);

        // Service 레이어 검증에서 MISSING_REQUIRED_FIELDS 발생
        willThrow(new CustomException(ErrorCode.MISSING_REQUIRED_FIELDS))
                .given(tripService).createPastTrip(any(CreatePastTripRequest.class), eq(userId));

        // when & then
        mockMvc.perform(post("/api/users/me/timeline/past")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void 과거_여행_생성_countryCodes_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreatePastTripRequest request = new CreatePastTripRequest();
        request.setTripName("유럽 배낭여행");
        request.setStartDate(LocalDate.of(2023, 5, 1));
        request.setEndDate(LocalDate.of(2023, 5, 14));
        request.setCountryCodes(null); // 필수 필드 null
        request.setEmotionId(2L);

        // Service 레이어 검증에서 MISSING_REQUIRED_FIELDS 발생
        willThrow(new CustomException(ErrorCode.MISSING_REQUIRED_FIELDS))
                .given(tripService).createPastTrip(any(CreatePastTripRequest.class), eq(userId));

        // when & then
        mockMvc.perform(post("/api/users/me/timeline/past")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void 과거_여행_생성_countryCodes_empty_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreatePastTripRequest request = new CreatePastTripRequest();
        request.setTripName("유럽 배낭여행");
        request.setStartDate(LocalDate.of(2023, 5, 1));
        request.setEndDate(LocalDate.of(2023, 5, 14));
        request.setCountryCodes(List.of()); // 빈 리스트
        request.setEmotionId(2L);

        // Service 레이어 검증에서 MISSING_REQUIRED_FIELDS 발생
        willThrow(new CustomException(ErrorCode.MISSING_REQUIRED_FIELDS))
                .given(tripService).createPastTrip(any(CreatePastTripRequest.class), eq(userId));

        // when & then
        mockMvc.perform(post("/api/users/me/timeline/past")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.MISSING_REQUIRED_FIELDS.getCode()));
    }
}

