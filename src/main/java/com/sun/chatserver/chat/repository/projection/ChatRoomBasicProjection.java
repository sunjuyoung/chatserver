package com.sun.chatserver.chat.repository.projection;

import java.time.LocalDateTime;

public interface ChatRoomBasicProjection {
    Long getRoomId();
    String getRoomName();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
