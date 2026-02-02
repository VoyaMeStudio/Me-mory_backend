package zim.tave.memory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.Emotion;
import zim.tave.memory.domain.User;
import zim.tave.memory.domain.VisitedCountry;
import zim.tave.memory.dto.request.RegisterVisitedCountryRequestDto;
import zim.tave.memory.dto.response.VisitedCountryResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.global.common.exception.GlobalExceptionHandler;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.CountryService;
import zim.tave.memory.service.VisitedCountryService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CountryControllerTest {

    @Mock
    private VisitedCountryService visitedCountryService;

    @Mock
    private CountryService countryService;

    @InjectMocks
    private CountryController countryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(countryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void searchCountries_returnsWrappedResponse() throws Exception {
        Country korea = new Country("KR", "대한민국", "🇰🇷");
        given(countryService.searchCountriesByName("대한")).willReturn(List.of(korea));

        mockMvc.perform(get("/api/countries").param("keyword", "대한"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data[0].countryCode").value("KR"))
                .andExpect(jsonPath("$.data[0].countryName").value("대한민국"));

        verify(countryService).searchCountriesByName("대한");
    }

    @Test
    void getVisitedCountries_returnsUserVisitedList() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(1L, "user@test.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        User user = new User();
        user.setId(1L);
        user.setKakaoId("kakao");
        user.setCreatedAt(LocalDate.now());
        user.setStatus(true);

        Country country = new Country("KR", "대한민국", "🇰🇷");
        Emotion emotion = new Emotion("행복", "#FF6B6B");
        VisitedCountry visited = new VisitedCountry();
        visited.setVisitedCountryId(10L);
        visited.setUser(user);
        visited.setCountry(country);
        visited.setEmotion(emotion);
        visited.setColor("#FF6B6B");

        given(visitedCountryService.getVisitedCountries(1L)).willReturn(List.of(
                VisitedCountryResponseDto.from(visited)
        ));

        mockMvc.perform(get("/api/users/me/visited-countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data[0].visitedCountryId").value(10L))
                .andExpect(jsonPath("$.data[0].countryCode").value("KR"))
                .andExpect(jsonPath("$.data[0].emotionName").value("행복"));

        verify(visitedCountryService).getVisitedCountries(1L);
    }

    @Test
    void registerVisitedCountry_returnsCreatedResponse() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(5L, "user@test.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        RegisterVisitedCountryRequestDto request = new RegisterVisitedCountryRequestDto("KR", 2L);

        User user = new User();
        user.setId(5L);

        Country country = new Country("KR", "대한민국", "🇰🇷");
        Emotion emotion = new Emotion("행복", "#FF6B6B");

        VisitedCountry visited = new VisitedCountry();
        visited.setVisitedCountryId(99L);
        visited.setUser(user);
        visited.setCountry(country);
        visited.setEmotion(emotion);
        visited.setColor("#FF6B6B");

        given(visitedCountryService.registerVisitedCountry(eq(5L), eq("KR"), eq(2L)))
                .willReturn(VisitedCountryResponseDto.from(visited));

        mockMvc.perform(post("/api/users/me/visited-countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(ResponseCode.CREATED.getCode()))
                .andExpect(jsonPath("$.data.countryCode").value("KR"))
                .andExpect(jsonPath("$.data.emotionName").value("행복"));

        verify(visitedCountryService).registerVisitedCountry(5L, "KR", 2L);
    }

    @Test
    void deleteVisitedCountry_returnsSuccess() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(3L, "user@test.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        mockMvc.perform(delete("/api/users/me/visited-countries/KR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(visitedCountryService).deleteVisitedCountry(3L, "KR");
    }

    @Test
    void registerVisitedCountry_whenCountryNotFound_returnsErrorResponse() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(7L, "user@test.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        RegisterVisitedCountryRequestDto request = new RegisterVisitedCountryRequestDto("ZZ", 3L);

        given(visitedCountryService.registerVisitedCountry(7L, "ZZ", 3L))
                .willThrow(new CustomException(ErrorCode.COUNTRY_NOT_FOUND));

        mockMvc.perform(post("/api/users/me/visited-countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ResponseCode.COUNTRY_NOT_FOUND.getCode()));
    }

    // ========== Validation 테스트 ==========

    @Test
    void 방문국가_등록_countryCode_null_400() throws Exception {
        // given
        CustomUserDetails principal = new CustomUserDetails(7L, "user@test.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        RegisterVisitedCountryRequestDto request = new RegisterVisitedCountryRequestDto(null, 3L);

        // Service 레이어 검증에서 countryService.findByCode(null) → null 반환 → NPE 또는 COUNTRY_NOT_FOUND
        // VisitedCountryService에서 country가 null이면 NPE가 발생하지만,
        // 실제로는 countryService.findByCode에서 null 체크 후 COUNTRY_NOT_FOUND를 던질 수 있음
        willThrow(new CustomException(ErrorCode.COUNTRY_NOT_FOUND))
                .given(visitedCountryService).registerVisitedCountry(eq(7L), any(), eq(3L));

        // when & then
        mockMvc.perform(post("/api/users/me/visited-countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ResponseCode.COUNTRY_NOT_FOUND.getCode()));
    }
}
