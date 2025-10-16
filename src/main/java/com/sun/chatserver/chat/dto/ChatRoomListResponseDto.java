package com.sun.chatserver.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sun.chatserver.chat.repository.projection.ChatRoomListProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 채팅방 목록 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListResponseDto {

    private String roomId;
    
    private String roomName;
    
    private List<String> chatRoomMembers;
    
    private LastMessageDto lastMessage;
    
    private Integer unreadCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 마지막 메시지 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastMessageDto {
        
        private String id;
        
        private String senderName;
        
        private String content;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }
    
    /**
     * Projection을 DTO로 변환하는 정적 팩토리 메서드
     * 
     * @param projection ChatRoomListProjection
     * @return ChatRoomListResponseDto
     */
    public static ChatRoomListResponseDto from(ChatRoomListProjection projection) {
        // 채팅방 멤버 이메일 목록 파싱 (쉼표로 구분된 문자열을 List로 변환)
        List<String> members = parseChatRoomMembers(projection.getChatRoomMembers());
        
        // 마지막 메시지 DTO 생성
        LastMessageDto lastMessage = null;
        if (projection.getLastMessageId() != null) {
            lastMessage = LastMessageDto.builder()
                    .id(String.valueOf(projection.getLastMessageId()))
                    .senderName(projection.getLastMessageSenderName())
                    .content(projection.getLastMessageContent())
                    .createdAt(projection.getLastMessageCreatedAt())
                    .build();
        }
        
        return ChatRoomListResponseDto.builder()
                .roomId(String.valueOf(projection.getRoomId()))
                .roomName(projection.getRoomName())
                .chatRoomMembers(members)
                .lastMessage(lastMessage)
                .unreadCount(projection.getUnreadCount() != null ? projection.getUnreadCount() : 0)
                .createdAt(projection.getCreatedAt())
                .updatedAt(projection.getUpdatedAt())
                .build();
    }
    
    /**
     * 쉼표로 구분된 이메일 문자열을 List로 변환
     * 
     * @param membersString 쉼표로 구분된 이메일 문자열
     * @return 이메일 목록
     */
    private static List<String> parseChatRoomMembers(String membersString) {
        if (membersString == null || membersString.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(membersString.split(","));
    }
}
