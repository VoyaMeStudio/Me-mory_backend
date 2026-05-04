package zim.tave.memory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import zim.tave.memory.dto.request.UpdateUserRequestDto;
import zim.tave.memory.dto.response.UserResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.security.JwtAuthenticationFilter;
import zim.tave.memory.service.SettingService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SettingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SettingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SettingService settingService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    private void setSecurityContext(Long userId) {
        CustomUserDetails principal = new CustomUserDetails(userId, "kakao_test");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, null);

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void 회원정보_수정_성공() throws Exception {
        setSecurityContext(1L);

        UpdateUserRequestDto request = UpdateUserRequestDto.builder()
                .surName("NEW")
                .profileImageUrl("https://new.com")
                .build();

        UserResponseDto response = new UserResponseDto(
                1L, "kakaoId", "https://new.com",
                "NEW", "FIRST", "한글",
                LocalDate.of(2000,1,1),
                "KOREA", 0L, 0L, "", true
        );

        when(settingService.updateUserInfo(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://new.com"));
    }

    @Test
    void 회원정보_수정_필드없음_예외() throws Exception {
        setSecurityContext(1L);

        UpdateUserRequestDto request = UpdateUserRequestDto.builder().build();

        when(settingService.updateUserInfo(eq(1L), any()))
                .thenThrow(new CustomException(ErrorCode.NO_FIELDS_TO_UPDATE));

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 회원정보_수정_사용자없음() throws Exception {
        setSecurityContext(1L);

        UpdateUserRequestDto request = UpdateUserRequestDto.builder()
                .profileImageUrl("test")
                .build();

        when(settingService.updateUserInfo(eq(1L), any()))
                .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 회원탈퇴_성공() throws Exception {
        setSecurityContext(1L);

        doNothing().when(settingService).deleteAccount(1L);

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isOk());
    }

    @Test
    void 회원탈퇴_사용자없음() throws Exception {
        setSecurityContext(1L);

        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND))
                .when(settingService).deleteAccount(1L);

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isNotFound());
    }

}
