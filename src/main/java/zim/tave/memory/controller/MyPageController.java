package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zim.tave.memory.dto.MyPageResponseDto;
import zim.tave.memory.global.common.ApiResponseDto;
import zim.tave.memory.global.common.ResponseCode;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.MyPageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
@Tag(name = "MyPage-Controller", description = "마이페이지 조회")
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "마이페이지 조회", description = "JWT 토큰 이용하여 마이페이지 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "마이페이지 조회 성공",
                    content = @Content(schema = @Schema(implementation = MyPageResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 누락 또는 만료)", content = @Content()),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자", content = @Content()),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content())
    })
    @GetMapping
    public ResponseEntity<ApiResponseDto<MyPageResponseDto>> getMyPage(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        MyPageResponseDto responseDto = myPageService.getMyPage(userId);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, responseDto));
    }

    /*
    //에러 테스트 코드
    @GetMapping("/test-error")
    public ResponseEntity<ApiResponseDto<MyPageResponseDto>> testError(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        MyPageResponseDto responseDto = myPageService.getErrorExample(userId);

        return ResponseEntity.ok(ApiResponseDto.success(ResponseCode.SUCCESS, responseDto));
    }
     */
}
