package com.sun.chatserver.chat.config;

import com.sun.chatserver.chat.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
//스프링과 stomp는 기본적으로 세션관리를 내부적으로 자동 처리
//추가로 이벤트 리스너를 만들어서 세션 연결, 해제 이벤트를 처리할 수 있음
//연결된 세션수를 실시간으로 확인 , 로그 디버깅 등에 활용 가능

@Slf4j
@Component
@RequiredArgsConstructor
public class StompEventListener {

    private final WebSocketSessionManager webSocketSessionManager;

    @EventListener
    public void connectHandler(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        webSocketSessionManager.addSession(sessionId);
       // sessions.add(accessor.getSessionId());
        log.info("Connected to stomp session {}", sessionId);
    }


    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // 예: "/topic/456" (채팅방 ID가 456)
        String destination = accessor.getDestination();

        if (destination.startsWith("/topic/")) {
            String roomId = extractRoomId(destination);  // "456" 추출
            webSocketSessionManager.addSubscription(sessionId, roomId);  // ← 저장
            log.info("Session {} subscribed to room {}", sessionId, roomId);
        }
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith("/topic/")) {
            String roomId = extractRoomId(destination);
            webSocketSessionManager.removeSubscription(sessionId, roomId);
            log.info("Session {} unsubscribed from room {}", sessionId, roomId);
        }
    }

    private String extractRoomId(String destination) {
        return destination.substring("/topic/".length());
    }


    @EventListener
    public void disconnectHandler(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        webSocketSessionManager.removeSession(sessionId);
        log.info("Disconnected  to stomp session {}", sessionId);
    }
}
