package com.sun.chatserver.chat.repository;

import com.sun.chatserver.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    // 활성화된 멤버들 조회
    List<ChatRoomMember> findByChatRoomId(Long chatRoomId);

    boolean existsByChatRoomIdAndMemberId(Long roomId, Long id);
}
