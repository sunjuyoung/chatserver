package com.sun.chatserver.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

//이 의미는 JSON 객체에 매핑되지 않은 속성이 있어도 무시하라는 뜻이다.
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageDto {
//{"id":"msg-1760061729678-g4mnc1ug1","roomId":"1","senderId":"test1@test.com","senderName":"syseoz1","content":"hi","timestamp":"2025-10-10T02:02:09.678Z","isRead":false,"type":"text"}
    private String id;
    private String roomId;
    private String senderId;
    private String senderName;
    private String content;
    private String timestamp;
    private boolean isRead;
    private String type;


}
