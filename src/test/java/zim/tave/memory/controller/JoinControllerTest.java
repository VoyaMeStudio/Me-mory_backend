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
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.JoinRequestDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.jwt.JwtUtil;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.security.JwtAuthenticationFilter;
import zim.tave.memory.service.JoinService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = JoinController.class)
@AutoConfigureMockMvc(addFilters = false)
public class JoinControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    JoinService joinService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    JwtUtil jwtUtil;

    private void setSecurityContext(Long userId) {
        CustomUserDetails principal = new CustomUserDetails(userId, "kakao_test");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, null);

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void 회원가입_성공() throws Exception {
        setSecurityContext(1L);

        JoinRequestDto request = new JoinRequestDto(
                "KANG",
                "JIHYE",
                "강지혜",
                LocalDate.of(2000, 1, 1),
                "REPUBLIC OF KOREA"
        );

        User user = new User();
        user.setId(1L);
        user.setSurName("KANG");
        user.setFirstName("JIHYE");
        user.setKoreanName("강지혜");
        user.setNationality("REPUBLIC OF KOREA");
        user.setBirth(LocalDate.of(2000, 1, 1));
        user.setRegistered(true);

        Mockito.when(joinService.join(eq(1L), any(JoinRequestDto.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.JOIN_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.firstName").value("JIHYE"));
    }

    @Test
    void 회원가입_중복가입_409() throws Exception {
        setSecurityContext(1L);

        JoinRequestDto request = new JoinRequestDto(
                "KANG", "JIHYE", "강지혜",
                LocalDate.of(2000, 1, 1),
                "REPUBLIC OF KOREA"
        );

        Mockito.when(joinService.join(eq(1L), any()))
                .thenThrow(new CustomException(ErrorCode.ALREADY_JOINED));

        mockMvc.perform(post("/api/auth/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ResponseCode.ALREADY_JOINED.getCode()))
                .andExpect(jsonPath("$.message").value(containsString(ResponseCode.ALREADY_JOINED.getMessage())));
    }

    @Test
    void 회원가입_카카오로그인필요_400() throws Exception {
        setSecurityContext(1L);

        JoinRequestDto request = new JoinRequestDto(
                "KANG", "JIHYE", "강지혜",
                LocalDate.of(2000, 1, 1),
                "REPUBLIC OF KOREA"
        );

        Mockito.when(joinService.join(eq(1L), any()))
                .thenThrow(new CustomException(ErrorCode.KAKAO_LOGIN_REQUIRED));

        mockMvc.perform(post("/api/auth/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResponseCode.KAKAO_LOGIN_REQUIRED.getCode()))
                .andExpect(jsonPath("$.message").value(containsString(ResponseCode.KAKAO_LOGIN_REQUIRED.getMessage())));
    }
}
