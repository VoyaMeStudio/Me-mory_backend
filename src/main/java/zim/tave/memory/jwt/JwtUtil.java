package zim.tave.memory.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import zim.tave.memory.domain.User;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.UserRepository;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long accessTokenExpirationMs = 1000 * 60 * 60; // 1시간
    private final long refreshTokenExpirationMs = 1000 * 60 * 60 * 24 * 14; // 14일

    // Access Token 생성
    public String generateAccessToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", "access")
                .setId(UUID.randomUUID().toString()) // JTI 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(key)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "refresh")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(key)
                .compact();
    }

    // userId 가져오기
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    // username 가져오기
    public String getUsernameFromToken(String token) {
        return parseClaims(token).get("username", String.class);
    }

    // 토큰 타입 확인
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = parseClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    // 유효성 검사
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("올바르지 않은 JWT 형식입니다: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰입니다: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있거나 null입니다: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("JWT 토큰 검증 중 오류가 발생했습니다: {}", e.getMessage());
            return false;
        }
    }

    // 유효성 검사 - 예외를 던지는 버전 (상세한 에러 처리가 필요한 경우)
    public void validateTokenWithException(String token) {
        try {
            parseClaims(token);
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException e) {
            log.warn("올바르지 않은 JWT 형식입니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.MALFORMED_TOKEN);
        } catch (SignatureException e) {
            log.warn("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN_SIGNATURE);
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰입니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있거나 null입니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (JwtException e) {
            log.warn("JWT 토큰 검증 중 오류가 발생했습니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    // Access Token인지 검증
    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Refresh Token인지 검증
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Profile("local")
    @Component
    @RequiredArgsConstructor
    public class TestTokenGenerator {

        private final JwtUtil jwtUtil;
        private final UserRepository userRepository;

        @EventListener(ApplicationReadyEvent.class)
        public void printTestUserToken() {
            User user = userRepository.findByKakaoId("test_강지혜")
                    .orElseThrow(() -> new RuntimeException("테스트 유저 없음"));
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getKakaoId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());
            System.out.println("🧩 테스트 유저 Access Token: " + accessToken);
            System.out.println("🔄 테스트 유저 Refresh Token: " + refreshToken);
        }
    }

}
