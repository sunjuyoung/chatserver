package com.sun.chatserver.member.security.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String secretKey;

    private final int expiration;
    private final int refreshExpiration = (int) Duration.ofDays(7).toMillis(); // 7일

    private SecretKey SECRET_KEY;

    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey,
                            @Value("${jwt.expiration}") int expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
        this.SECRET_KEY = new SecretKeySpec(Base64.getDecoder().decode(secretKey),
                SignatureAlgorithm.HS256.getJcaName());
    }

    //refresh token 용
    public String createRefreshToken(String email, String role, Long userId, String name) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);
        return jwtBuilder(email, role, userId, name, now, expiryDate);
    }

    public String createToken(String email, String role, Long userId, String name) {
        Date now = new Date();
        Date expiryDate = new Date(expiration);

        return jwtBuilder(email, role, userId, name, now, expiryDate);
    }

    private String jwtBuilder(String email, String role, Long userId, String name, Date now, Date expiryDate) {
        return Jwts.builder()
                .subject(email)                             // JWT subject에 email 저장
                .claim("role", role)
                .claim("userId", userId)
                .claim("name", name)
                .issuedAt(now)                              // 토큰 발행 시간
                .expiration(expiryDate)                     // 토큰 만료 시간
                .signWith(SECRET_KEY, Jwts.SIG.HS256)       // 0.12.x 버전 방식
                .compact();
    }

    // JWT 토큰에서 email 추출
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    // JWT 토큰에서 role 추출
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("role", String.class);
    }

    //jwt 토큰에서 userId 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userId", Long.class);
    }

    // JWT 토큰에서 모든 claims 추출
    public Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // JWT 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JWT 검증 실패 (만료, 서명 오류, 잘못된 형식 등)

            return false;
        }
    }

    // JWT 토큰 만료 확인
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
