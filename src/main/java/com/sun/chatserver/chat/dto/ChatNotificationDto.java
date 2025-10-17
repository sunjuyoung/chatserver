package com.sun.chatserver.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatNotificationDto {

    private String type;
    private Long roomId;
    private Long unreadCount;
    private String message;
}
