package com.sun.chatserver.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

//@Configuration
//@EnableWebSocket
//@RequiredArgsConstructor
//public class WebsocketConfig implements WebSocketConfigurer {
//
//    private final SimpleWebSocketHandler simpleWebSocketHandler;
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//
//        //connect url로 websocket 연결 요청이 들어오면 핸들러 클래스가 처리
//        registry.addHandler(simpleWebSocketHandler,"/connect")
//                //securityConfig에서 cors 설정을 해주었지만 그건 http 요청에 대한 cors 설정이므로
//                //websocket 요청에 대한 cors 설정도 해주어야함
//                .setAllowedOrigins("http://localhost:3001");
//    }
//}
