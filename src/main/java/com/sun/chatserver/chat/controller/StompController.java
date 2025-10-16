package com.sun.chatserver.chat.controller;

import com.sun.chatserver.chat.dto.ChatMessageDto;
import com.sun.chatserver.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompController {

    private final org.springframework.messaging.simp.SimpMessagingTemplate messageTemplate;
    private final ChatService chatService;

    //방법1. 메시지를 받아서 다시 클라이언트에게 보내는 역할


    //DestinationVariable @MessageMapping 어노테이션의 경로 변수 값을 메서드 매개변수에 바인딩
    //Payload 클라이언트가 보낸 메시지의 본문을 메서드 매개변수에 바인딩
    // /publish/{roomId}로 메시지가 발행되면 이 메서드가 호출됨
    //roomId는 채팅방 id
    //message는 클라이언트가 보낸 메시지
//    @MessageMapping("/{roomId}")
//    @SendTo("/topic/{roomId}") //해당 roomid에 메시지를 발행 ,구독자에게 메시지를 전송할 경로
//    public String sendMessage(@DestinationVariable String roomId, @Payload String message) {
//
//        log.info("Received message: {} for roomId: {}", message, roomId);
//
//        return message;
//
//    }
    //방법2 MessageMapping 만 사용
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageDto) {
        log.info("Received message: {} for roomId: {}", chatMessageDto, roomId);
        Long saveMessage = chatService.saveMessage(roomId, chatMessageDto);
        chatMessageDto.setId(String.valueOf(saveMessage));

        //STOMP 메시지를 특정 (채팅방) 구독자들에게 발행
        messageTemplate.convertAndSend("/topic/" + roomId, chatMessageDto);
    }

}
