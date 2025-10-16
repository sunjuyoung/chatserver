package com.sun.chatserver.chat.config;

import com.sun.chatserver.chat.service.ChatService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Component
public class StompHandler implements ChannelInterceptor {

    private final String secretKey;

    private SecretKey SECRET_KEY;

    private final ChatService chatService;

    public StompHandler(@Value("${jwt.secretKey}") String secretKey, ChatService chatService) {
        this.chatService = chatService;
        this.secretKey = secretKey;
        //인코딩되어있는 키를 디코딩 하고 암호화 알고리즘을 적용
        this.SECRET_KEY = new SecretKeySpec(Base64.getDecoder().decode(secretKey),
                SignatureAlgorithm.HS256.getJcaName());
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT.equals(accessor.getCommand())) {
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            // WebSocket 연결 시 JWT 토큰 검증 로직 추가
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("No JWT token found in request headers");
            }
            String token = bearerToken.substring(7);

            tokenValid(token);


        }
        if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Subscribe No JWT token found in request headers");
            }
            String token = bearerToken.substring(7);

            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getBody();
            String email = claims.getSubject();
            String roomId = accessor.getDestination().split("/")[2];
            boolean is = chatService.isRoomMember(Long.parseLong(roomId), email);
        }
        return message;

    }

    private void tokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            // JWT 검증 실패 (만료, 서명 오류, 잘못된 형식 등)
            log.error("Invalid JWT token: {}", e.getMessage());

        }
    }
}
