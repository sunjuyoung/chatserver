package com.sun.chatserver.chat.service;

import com.sun.chatserver.chat.domain.ChatMessage;
import com.sun.chatserver.chat.domain.ChatRoom;
import com.sun.chatserver.chat.dto.ChatMessageDto;
import com.sun.chatserver.chat.dto.ChatRoomListDto;
import com.sun.chatserver.chat.dto.ChatRoomListResponseDto;
import com.sun.chatserver.chat.repository.ChatMessageRepository;
import com.sun.chatserver.chat.repository.ChatRoomMemberRepository;
import com.sun.chatserver.chat.repository.ChatRoomRepository;
import com.sun.chatserver.chat.repository.ReadStatusRepository;
import com.sun.chatserver.chat.repository.projection.*;
import com.sun.chatserver.member.domain.Member;
import com.sun.chatserver.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 채팅방 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ReadStatusRepository readStatusRepository;


    @Transactional(readOnly = true)
    public List<ChatRoomListDto> getAllChatRoomList(Long lastChatRoomId) {
        List<ChatRoom> chatRooms = lastChatRoomId == null ?
                chatRoomRepository.findAllChatRoom() :
                chatRoomRepository.findAllChatRoom(lastChatRoomId);

        List<Long> roomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .toList();

        List<ChatRoomMembersProjection> membersInfo =
                chatRoomRepository.findMembersByRoomIds(roomIds);

        Map<Long, Integer> membersMap = membersInfo.stream()
                .collect(Collectors.toMap(
                        ChatRoomMembersProjection::getRoomId,
                        ChatRoomMembersProjection::getMemberCount
                ));

        //DTO 조합
        return chatRooms.stream()
                .map(room -> {
                    Long roomId = room.getId();
                    return ChatRoomListDto.builder()
                            .roomId(roomId)
                            .roomName(room.getName())
                            .memberCount(membersMap.getOrDefault(roomId, 0))
                            .lastMessageId(null)
                            .lastMessageSenderName(null)
                            .lastMessageContent(null)
                            .lastMessageCreatedAt(null)
                            .unreadCount(0) //전체 조회이므로 0으로 설정
                            .createdAt(room.getCreatedAt())
                            .updatedAt(room.getUpdatedAt())
                            .build();
                })
                .toList();


    }



    @Transactional(readOnly = true)
    public List<ChatRoomListDto> getChatRoomListByMemberId(Long memberId) {
        // 1. 기본 채팅방 정보 조회
        List<ChatRoomBasicProjection> basicRooms =
                chatRoomRepository.findBasicChatRoomsByMemberId(memberId);

        if (basicRooms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roomIds = basicRooms.stream()
                .map(ChatRoomBasicProjection::getRoomId)
                .toList();

        // 2. 채팅방별 추가 정보 조회 (병렬 실행 가능)
        List<ChatRoomMembersProjection> membersInfo =
                chatRoomRepository.findMembersByRoomIds(roomIds);
        List<LastMessageProjection> lastMessages =
                chatRoomRepository.findLastMessagesByRoomIds(roomIds);
        List<UnreadCountProjection> unreadCounts =
                readStatusRepository.findUnreadCountsByRoomIds(roomIds, memberId);


        // 3. Map으로 변환 (O(1) 조회를 위해)
        Map<Long, Integer> membersMap = membersInfo.stream()
                .collect(Collectors.toMap(
                        ChatRoomMembersProjection::getRoomId,
                        ChatRoomMembersProjection::getMemberCount
                ));

        Map<Long, LastMessageProjection> lastMessageMap = lastMessages.stream()
                .collect(Collectors.toMap(LastMessageProjection::getRoomId, Function.identity()));

        Map<Long, Integer> unreadCountMap = unreadCounts.stream()
                .collect(Collectors.toMap(
                        UnreadCountProjection::getRoomId,
                        UnreadCountProjection::getUnreadCount
                ));

        // 4. DTO 조합
        List<ChatRoomListDto> result = basicRooms.stream()
                .map(room -> {
                    Long roomId = room.getRoomId();
                    LastMessageProjection lastMsg = lastMessageMap.get(roomId);

                    return ChatRoomListDto.builder()
                            .roomId(roomId)
                            .roomName(room.getRoomName())
                            .memberCount(membersMap.getOrDefault(roomId, 0))
                            .lastMessageId(lastMsg != null ? lastMsg.getLastMessageId() : null)
                            .lastMessageSenderName(lastMsg != null ? lastMsg.getLastMessageSenderName() : null)
                            .lastMessageContent(lastMsg != null ? lastMsg.getLastMessageContent() : null)
                            .lastMessageCreatedAt(lastMsg != null ? lastMsg.getLastMessageCreatedAt() : null)
                            .unreadCount(unreadCountMap.getOrDefault(roomId, 0))
                            .createdAt(room.getCreatedAt())
                            .updatedAt(room.getUpdatedAt())
                            .build();
                })
                .sorted((a, b) -> {
                    // 최근 메시지 시간 기준 정렬
                    LocalDateTime timeA = a.getLastMessageCreatedAt() != null
                            ? a.getLastMessageCreatedAt() : a.getUpdatedAt();
                    LocalDateTime timeB = b.getLastMessageCreatedAt() != null
                            ? b.getLastMessageCreatedAt() : b.getUpdatedAt();
                    return timeB.compareTo(timeA);
                })
                .toList();

        return result;
    }



    /**
     * 특정 사용자의 채팅방 목록 조회
     * 
     * @param memberId 사용자 ID
     * @return 채팅방 목록 DTO
     */
    public List<ChatRoomListResponseDto> getChatRoomList(Long memberId) {
        // Repository에서 Projection 조회
        List<ChatRoomListProjection> projections = chatRoomRepository.findChatRoomListByMemberId(memberId);
        
        // Projection을 DTO로 변환
        return projections.stream()
                .map(ChatRoomListResponseDto::from)
                .collect(Collectors.toList());
    }
    



    public List<ChatMessageDto> getPreviousMessages(Long roomId) {
        //해당 방에 참여자 인지 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 사용자 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 채팅방 참여 여부 확인
        boolean isMember = chatRoomMemberRepository.existsByChatRoomIdAndMemberId(roomId, member.getId());
        if (!isMember) {
            throw new RuntimeException("해당 채팅방에 참여하지 않은 사용자입니다.");
        }

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        //특정 룸에 대한 이전 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);

        // ChatMessage를 ChatMessageDto로 변환
        return messages.stream()
                .map(message -> {
                    // 현재 사용자의 해당 메시지 읽음 상태 확인
//                    boolean isRead = readStatusRepository.findByChatMessageIdAndMemberId(message.getId(), member.getId())
//                            .map(readStatus -> readStatus.getIsRead())
//                            .orElse(false);

                    return ChatMessageDto.builder()
                            .id(String.valueOf(message.getId()))
                            .roomId(String.valueOf(message.getChatRoom().getId()))
                            .senderId(message.getSender().getEmail())
                            .senderName(message.getSender().getName())
                            .content(message.getContent())
                            .timestamp(message.getCreatedAt().toString())
                            .isRead(true)
                            .type("text")
                            .build();
                })
                .collect(Collectors.toList());
    }
}
