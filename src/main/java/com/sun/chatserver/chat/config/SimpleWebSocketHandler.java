package com.sun.chatserver.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//TextWebSocketHandler : 웹소켓에서 텍스트 메시지를 처리하기 위한 기본 핸들러 클래스
//핸들러에 등록될수있는 객체
//connect로 들어오는 요청을 처리
//@Slf4j
//@Component
//public class SimpleWebSocketHandler extends TextWebSocketHandler {

    //동시성 문제 해결을 위해 ConcurrentHashMap 사용
//    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        sessions.add(session);
//        log.info("New WebSocket connection established. Session ID: {}", session.getId());
//
//    }
//
//
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        log.info("Received message: {} from Session ID: {}", payload, session.getId());
//        for(WebSocketSession s : sessions){
//            if(s.isOpen()){
//                s.sendMessage(new TextMessage(payload));
//            }
//        }
//
//    }
//
//
//
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        super.afterConnectionClosed(session, status);
//    }
//

//}
