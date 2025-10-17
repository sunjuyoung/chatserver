package com.sun.chatserver.chat.service;

import com.sun.chatserver.chat.domain.*;
import com.sun.chatserver.chat.dto.ChatMessageDto;
import com.sun.chatserver.chat.dto.ChatRoomCreateDto;
import com.sun.chatserver.chat.dto.ChatRoomListResDto;
import com.sun.chatserver.chat.repository.ChatMessageRepository;
import com.sun.chatserver.chat.repository.ChatRoomMemberRepository;
import com.sun.chatserver.chat.repository.ChatRoomRepository;
import com.sun.chatserver.chat.repository.ReadStatusRepository;
import com.sun.chatserver.member.domain.Member;
import com.sun.chatserver.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;



    public Long saveMessage(Long roomId, ChatMessageDto chatMessageDto) {
        //채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));

        //보낸 사람 조회
        Member member = memberRepository.findByEmail(chatMessageDto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid sender ID"));


        //메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(member)
                .chatRoom(chatRoom)
                .content(chatMessageDto.getContent())
                .build();
        ChatMessage message = chatMessageRepository.save(chatMessage);

        //사용자별 읽음 여부 저장
        chatRoomMemberRepository.findByChatRoomId(roomId).forEach(chatRoomMember -> {
            ReadStatus read = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(chatRoomMember.getMember())
                    .chatMessage(chatMessage)
                    .isRead(chatRoomMember.getMember().equals(member)) //일단 보낸 사람만 읽음 처리
                    .build();
            readStatusRepository.save(read);

        });

        return message.getId();
    }

    //방장은 따로 만들지 않는다.
    //개설자는 자동으로 채팅방 멤버로 등록
    public Long createGroupRoom(ChatRoomCreateDto dto) {
        //securityContext 꺼내기 -> email
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String userEmail = securityContext.getAuthentication().getName();
        dto.setEmail(userEmail);
        log.info("Creating group room for user {}", userEmail);

        //채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(dto.getName())
                .build();

        ChatRoom room = chatRoomRepository.save(chatRoom);

        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email"));

        //채팅방 멤버로 등록
        ChatRoomMember roomMember = ChatRoomMember.builder()
                .member(member)
                .chatRoom(room)

                .build();

        chatRoomMemberRepository.save(roomMember);
        return room.getId();
    }

    //활성화된 그룹 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<ChatRoomListResDto> getGroupChatRoomList() {

        List<ChatRoom> chatRooms =
                chatRoomRepository.findByTypeAndIsActiveTrueOrderByCreatedAtDesc(ChatRoomType.GROUP);

        return chatRooms.stream()
                .map(room -> ChatRoomListResDto.builder()
                        .roomId(room.getId())
                        .roomName(room.getName())
                        .roomType(room.getType().name())
                        .build())
                .toList();

    }

    public void joinGroupChatRoom(Long roomId, String userEmail) {
        //채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));

        //멤버 조회
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email"));

        //이미 가입된 멤버인지 확인
        boolean isMemberExists = chatRoomMemberRepository.existsByChatRoomIdAndMemberId(roomId, member.getId());
        if (isMemberExists) {
            throw new IllegalArgumentException("Member already joined the chat room");
        }
        //채팅방 멤버로 등록
        ChatRoomMember roomMember = ChatRoomMember.builder()
                .member(member)
                .chatRoom(chatRoom)
                .build();
        chatRoomMemberRepository.save(roomMember);
    }

    public boolean isRoomMember(Long roomId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email"));
        return chatRoomMemberRepository.existsByChatRoomIdAndMemberId(roomId, member.getId());
    }

    public void markReadAllMessage(Long roomId) {
        String userEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email"));

        // 권한 체크 - 채팅방 멤버인지 확인
        if (!isRoomMember(roomId, userEmail)) {
            throw new IllegalArgumentException("User is not a member of this chat room");
        }

        // Bulk update로 읽지 않은 메시지 일괄 업데이트
        int updatedCount = readStatusRepository.bulkUpdateIsReadByChatRoomIdAndMember(roomId, member.getId());
        log.info("Marked {} messages as read for user {} in room {}", updatedCount, userEmail, roomId);
    }

    public void markReadMessage(Long roomId, String id) {

        Long messageId = Long.parseLong(id);

        chatMessageRepository.findById(messageId).ifPresent(message -> {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            String userEmail = securityContext.getAuthentication().getName();
            Member member = memberRepository.findByEmail(userEmail).orElseThrow(() -> new IllegalArgumentException("Invalid email"));
            ReadStatus readStatus = readStatusRepository.findByChatMessageAndMember(  message,member)
                    .orElseThrow(() -> new IllegalArgumentException("Read status not found"));
            if (!readStatus.getIsRead()) {
                readStatus.updateIsRead(true);


            }
        });
    }

    public  List<ChatRoomMember> incrementUnreadCount(Long roomId, String senderId) {


        //보낸 사람을 제외한 채팅방 멤버 조회
        return chatRoomMemberRepository.findByChatRoomId(roomId);

    }
}
