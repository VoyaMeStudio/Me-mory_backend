package zim.tave.memory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import zim.tave.memory.dto.request.LoginRequestDto;
import zim.tave.memory.dto.response.LoginResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.security.JwtAuthenticationFilter;
import zim.tave.memory.service.LoginService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LoginService loginService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    JwtUtil jwtUtil;

    @Test
    void 로그인_성공() throws Exception {
        // given
        LoginRequestDto request = new LoginRequestDto("valid_token");

        LoginResponseDto response = new LoginResponseDto(
                1L,
                true,
                "4317757086",
                "http://img1.kakaocdn.net/profile.jpeg",
                "jwt_token"
        );

        Mockito.when(loginService.login(any(LoginRequestDto.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.jwtAccessToken").value("jwt_token"));
    }

    @Test
    void 로그인_토큰누락_400() throws Exception {
        LoginRequestDto request = new LoginRequestDto("");

        Mockito.when(loginService.login(any()))
                .thenThrow(new CustomException(ErrorCode.KAKAO_TOKEN_MISSING));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.KAKAO_TOKEN_MISSING.getCode()))
                .andExpect(jsonPath("$.message").value(containsString(ResponseCode.KAKAO_TOKEN_MISSING.getMessage())));
    }

    @Test
    void 로그아웃_성공() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(1L, "kakao_123");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(patch("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        Mockito.verify(loginService).logout(1L);
    }
}
