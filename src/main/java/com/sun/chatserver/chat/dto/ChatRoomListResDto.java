package com.sun.chatserver.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomListResDto {

    private Long roomId;
    private String roomName;
    private String lastMessage;
    private int unreadCount;
    private String roomType; // DIRECT, GROUP, CHANNEL
}
