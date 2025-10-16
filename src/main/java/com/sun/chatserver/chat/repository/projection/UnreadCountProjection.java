package com.sun.chatserver.chat.repository.projection;

public interface UnreadCountProjection {
    Long getRoomId();
    Integer getUnreadCount();
}
