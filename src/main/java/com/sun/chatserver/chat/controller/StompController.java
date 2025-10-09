package com.sun.chatserver.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class StompController {

    //DestinationVariable @MessageMapping 어노테이션의 경로 변수 값을 메서드 매개변수에 바인딩
    //Payload 클라이언트가 보낸 메시지의 본문을 메서드 매개변수에 바인딩
    // /publish/{roomId}로 메시지가 발행되면 이 메서드가 호출됨
    //roomId는 채팅방 id
    //message는 클라이언트가 보낸 메시지
    @MessageMapping("/{roomId}")
    @SendTo("/topic/{roomId}") //해당 roomid에 메시지를 발행 ,구독자에게 메시지를 전송할 경로
    public String sendMessage(@DestinationVariable String roomId, @Payload String message) {

        log.info("Received message: {} for roomId: {}", message, roomId);

        return message;

    }
}
