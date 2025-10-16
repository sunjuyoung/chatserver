package com.sun.chatserver.chat.controller;

import com.sun.chatserver.chat.dto.ChatMessageDto;
import com.sun.chatserver.chat.dto.ChatRoomCreateDto;
import com.sun.chatserver.chat.dto.ChatRoomListResDto;
import com.sun.chatserver.chat.dto.JoinChatRoomDto;
import com.sun.chatserver.chat.service.ChatService;
import com.sun.chatserver.member.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    //그룹채팅방 개설
    //누구든 개설하고
    @PostMapping("/room/group/create")
    public ResponseEntity<Long> createGroupRoom(@RequestBody ChatRoomCreateDto chatRoomCreateDto) {
        Long roomId = chatService.createGroupRoom(chatRoomCreateDto);
        return ResponseEntity.ok().body(roomId);
    }

    //그룹채팅목록조회
    @GetMapping("/room/group/list")
    public ResponseEntity<ApiResponse> groupRoomList() {
        List<ChatRoomListResDto> list = chatService.getGroupChatRoomList();
        ApiResponse<List<ChatRoomListResDto>> response = ApiResponse.success("success", list);
        return ResponseEntity.ok(response);
    }

    //그룹채팅방 참여
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<ApiResponse> joinGroupRoom(@PathVariable("roomId") Long roomId, @RequestBody JoinChatRoomDto dto) {
        log.info("join room roomId={}, email={}", roomId, dto.getEmail());
        chatService.joinGroupChatRoom(roomId, dto.getEmail());
        return ResponseEntity.ok().build();
    }

    //채팅메시지 읽음처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<ApiResponse> readMessage(@PathVariable("roomId") Long roomId, @RequestBody ChatMessageDto dto) {
        chatService.markReadMessage(roomId, dto.getId());
        return ResponseEntity.ok().build();
    }

    //채팅메시지 이전 메시지 전체 읽음처리
    @PostMapping("/room/{roomId}/read-all")
    public ResponseEntity<ApiResponse> readAllMessage(@PathVariable("roomId") Long roomId) {
        chatService.markReadAllMessage(roomId);
        return ResponseEntity.ok().build();
    }



}
