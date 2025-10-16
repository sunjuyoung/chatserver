package com.sun.chatserver.chat.controller;

import com.sun.chatserver.chat.dto.ChatMessageDto;
import com.sun.chatserver.chat.dto.ChatRoomListDto;
import com.sun.chatserver.chat.dto.ChatRoomListResponseDto;
import com.sun.chatserver.chat.service.ChatRoomQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채팅방 조회 API 컨트롤러
 */
@RestController
@RequestMapping("/api/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomQueryController {

    private final ChatRoomQueryService chatRoomQueryService;

    /**
     * 이전 메시지 조회
     */
    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<ChatMessageDto>> getPreviousMessages(
            @PathVariable Long roomId){
        List<ChatMessageDto> messages = chatRoomQueryService.getPreviousMessages(roomId);
        return ResponseEntity.ok(messages);

    }

    /**
     * 현재 사용자의 채팅방 목록 조회
     * 
     * TODO: 실제 사용 시 @AuthenticationPrincipal 또는 SecurityContext에서 사용자 ID 가져오기
     * 
     * @param memberId 사용자 ID (임시로 파라미터로 받음)
     * @return 채팅방 목록
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomListDto>> getChatRoomList(
            @RequestParam Long memberId
    ) {
        List<ChatRoomListDto> chatRoomListByMemberId = chatRoomQueryService.getChatRoomListByMemberId(memberId);
        return ResponseEntity.ok(chatRoomListByMemberId);
    }
    
    /**
     * 전체 채팅방 목록 조회
     * - unreadCount는 0으로 반환됨 (특정 사용자 기준이 아니므로)
     * 
     * TODO: 페이징 처리 (lastChatRoomId 기반)
     * 
     * @return 전체 채팅방 목록
     */
    @GetMapping("/all")
    public ResponseEntity<List<ChatRoomListDto>> getAllChatRoomList(
            @RequestParam(value = "lastChatRoomId", required = false) Long lastChatRoomId
    ) {
        List<ChatRoomListDto> roomList = chatRoomQueryService.getAllChatRoomList(lastChatRoomId);
        return ResponseEntity.ok(roomList);
    }
    


}
