package com.sun.chatserver.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomListDto {

    private Long roomId;
    private String roomName;
    private Integer memberCount;
    private Long lastMessageId;
    private String lastMessageSenderName;
    private String lastMessageContent;
    private LocalDateTime lastMessageCreatedAt;
    private Integer unreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
