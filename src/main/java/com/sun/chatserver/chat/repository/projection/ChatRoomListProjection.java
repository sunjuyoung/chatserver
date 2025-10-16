package com.sun.chatserver.chat.repository.projection;

import java.time.LocalDateTime;

/**
 * 채팅방 목록 조회용 Projection Interface
 */
public interface ChatRoomListProjection {
    
    Long getRoomId();
    
    String getRoomName();
    
    String getChatRoomMembers();  // 쉼표로 구분된 이메일 문자열
    
    Long getLastMessageId();
    
    String getLastMessageSenderName();
    
    String getLastMessageContent();
    
    LocalDateTime getLastMessageCreatedAt();
    
    Integer getUnreadCount();
    
    LocalDateTime getCreatedAt();
    
    LocalDateTime getUpdatedAt();
}
