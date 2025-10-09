package com.sun.chatserver.member.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60; // 7일 (초)

    /**
     * 로그인 시 Refresh Token 저장
     */
    public void saveRefreshToken(String refreshToken, Long userId) {
        log.info("Saving refresh token for userId {}: {}", userId, refreshToken);
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(
                key,
                String.valueOf(userId),
                REFRESH_TOKEN_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
    }

    /**
     * Refresh Token으로 userId 조회 (검증)
     */
    public Optional<Long> getUserIdByRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(userId).map(Long::valueOf);
    }

    /**
     * Refresh Token 삭제 (로그아웃 시)
     */
    public void deleteRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.delete(key);
    }

    /**
     * 특정 유저의 모든 Refresh Token 삭제 (전체 로그아웃)
     */
    public void deleteAllRefreshTokensByUserId(Long userId) {
        Set<String> keys = redisTemplate.keys(REFRESH_TOKEN_PREFIX + "*");
        if (keys != null) {
            keys.forEach(key -> {
                String storedUserId = redisTemplate.opsForValue().get(key);
                if (String.valueOf(userId).equals(storedUserId)) {
                    redisTemplate.delete(key);
                }
            });
        }
    }
}