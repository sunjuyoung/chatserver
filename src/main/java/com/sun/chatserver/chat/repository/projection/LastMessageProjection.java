package com.sun.chatserver.chat.repository.projection;

import java.time.LocalDateTime;

public interface LastMessageProjection {
    Long getRoomId();
    Long getLastMessageId();
    String getLastMessageSenderName();
    String getLastMessageContent();
    LocalDateTime getLastMessageCreatedAt();
}
