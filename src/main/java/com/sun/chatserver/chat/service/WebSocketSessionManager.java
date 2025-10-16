package com.sun.chatserver.chat.service;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();


    //연결 세션 추가
    public void addSession(String sessionId) {
        sessionSubscriptions.putIfAbsent(sessionId, new HashSet<>());
    }

    // 구독 추가
    public void addSubscription(String sessionId, String roomId) {
        sessionSubscriptions.computeIfAbsent(sessionId, k -> new HashSet<>()).add(roomId);
    }

    // 구독 제거
    public void removeSubscription(String sessionId, String roomId) {
        Set<String> rooms = sessionSubscriptions.get(sessionId);
        if (rooms != null) {
            rooms.remove(roomId);
            if (rooms.isEmpty()) {
                sessionSubscriptions.remove(sessionId);
            }
        }
    }

    // 세션 전체 제거 (disconnect 시)
    public void removeSession(String sessionId) {
        sessionSubscriptions.remove(sessionId);
    }

    // 특정 세션이 특정 방을 구독 중인지 확인
    public boolean isSubscribed(String sessionId, String roomId) {
        Set<String> rooms = sessionSubscriptions.get(sessionId);
        return rooms != null && rooms.contains(roomId);
    }

    // 특정 방을 구독 중인 모든 세션 ID 조회
    public Set<String> getSubscribedSessions(String roomId) {
        return sessionSubscriptions.entrySet().stream()
                .filter(entry -> entry.getValue().contains(roomId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}

