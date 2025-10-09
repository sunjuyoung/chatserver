package com.sun.chatserver.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOrigins("http://localhost:3001")
                .withSockJS();// ws://가 아닌 http:// 엔드 포인트를 사용할 수 있도록 설정 (브라우저 호환성)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // /topic으로 시작하는 메시지를 수신
        registry.enableSimpleBroker("/topic"); //메시지 브로커의 접두사 설정

        // /publish로 시작하는 메시지를 발행 핸들러로 라우팅
        // publish시작하는 url패턴으로 메시지가 발행되면 @controller 객체의 @MessageMapping 어노테이션이 붙은 메서드로 라우팅
        // /publish/1  1은 roomId
        registry.setApplicationDestinationPrefixes("/publish"); //클라이언트가 메시지를 보낼 때 사용할 접두사 설정
    }
}
