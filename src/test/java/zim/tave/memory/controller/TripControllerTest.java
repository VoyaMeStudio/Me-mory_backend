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
import zim.tave.memory.dto.request.CreateTripRequest;
import zim.tave.memory.dto.request.UpdateTripRequest;
import zim.tave.memory.dto.response.TripResponseDto;
import zim.tave.memory.global.common.exception.GlobalExceptionHandler;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.DiaryService;
import zim.tave.memory.service.TripService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    @Mock
    private TripService tripService;

    @Mock
    private DiaryService diaryService;

    @InjectMocks
    private TripController tripController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tripController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reset(tripService, diaryService);
    }

    private void setSecurityContext(Long userId) {
        CustomUserDetails principal = new CustomUserDetails(userId, "test@test.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    @Test
    void 여행_생성_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreateTripRequest request = new CreateTripRequest();
        request.setTripName("제주도 여행");
        request.setDescription("제주도 3박 4일 여행");
        request.setThemeId(1L);
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 1, 4));

        TripResponseDto responseDto = TripResponseDto.builder()
                .id(1L)
                .tripName("제주도 여행")
                .description("제주도 3박 4일 여행")
                .build();

        given(tripService.createTrip(any(CreateTripRequest.class), eq(userId))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/users/me/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.tripName").value("제주도 여행"));

        verify(tripService).createTrip(any(CreateTripRequest.class), eq(userId));
    }

    @Test
    void 여행_수정_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 여행명");
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 1, 4));

        // when & then
        mockMvc.perform(patch("/api/users/me/trips/{tripId}", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tripService).updateTrip(eq(tripId), any(UpdateTripRequest.class), eq(userId));
    }

    @Test
    void 여행_목록_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        TripResponseDto trip1 = TripResponseDto.builder()
                .id(1L)
                .tripName("제주도 여행")
                .build();

        TripResponseDto trip2 = TripResponseDto.builder()
                .id(2L)
                .tripName("부산 여행")
                .build();

        given(tripService.getTripsByUserId(userId)).willReturn(List.of(trip1, trip2));

        // when & then
        mockMvc.perform(get("/api/users/me/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].tripName").value("제주도 여행"))
                .andExpect(jsonPath("$.data[1].tripName").value("부산 여행"));

        verify(tripService).getTripsByUserId(userId);
    }

    // ========== Validation 테스트 ==========

    @Test
    void 여행_생성_시작일_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreateTripRequest request = new CreateTripRequest();
        request.setTripName("제주도 여행");
        request.setStartDate(null); // @NotNull 위반
        request.setEndDate(LocalDate.of(2024, 1, 4));

        // when & then
        mockMvc.perform(post("/api/users/me/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 여행_생성_종료일_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreateTripRequest request = new CreateTripRequest();
        request.setTripName("제주도 여행");
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(null); // @NotNull 위반

        // when & then
        mockMvc.perform(post("/api/users/me/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 여행_생성_시작일과_종료일_모두_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        CreateTripRequest request = new CreateTripRequest();
        request.setTripName("제주도 여행");
        request.setStartDate(null); // @NotNull 위반
        request.setEndDate(null); // @NotNull 위반

        // when & then
        mockMvc.perform(post("/api/users/me/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 여행_수정_시작일_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 여행명");
        request.setStartDate(null); // @NotNull 위반
        request.setEndDate(LocalDate.of(2024, 1, 4));

        // when & then
        mockMvc.perform(patch("/api/users/me/trips/{tripId}", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 여행_수정_종료일_null_400() throws Exception {
        // given
        Long userId = 1L;
        setSecurityContext(userId);

        Long tripId = 1L;
        UpdateTripRequest request = new UpdateTripRequest();
        request.setTripName("수정된 여행명");
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(null); // @NotNull 위반

        // when & then
        mockMvc.perform(patch("/api/users/me/trips/{tripId}", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

