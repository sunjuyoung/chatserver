package com.sun.chatserver.chat.repository;

import com.sun.chatserver.chat.domain.ChatMessage;
import com.sun.chatserver.chat.domain.ChatRoom;
import com.sun.chatserver.chat.domain.ReadStatus;
import com.sun.chatserver.chat.repository.projection.UnreadCountProjection;
import com.sun.chatserver.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {

    Optional<ReadStatus> findByChatMessageIdAndMemberId(Long chatMessageId, Long memberId);
    List<ReadStatus> findByChatRoomAndMember(ChatRoom chatRoom, Member member);

    Optional<ReadStatus> findByChatMessageAndMember( ChatMessage chatMessage,Member member);

    @Modifying
    @Query("UPDATE ReadStatus rs SET rs.isRead = true " +
           "WHERE rs.chatRoom.id = :roomId " +
           "AND rs.member.id = :memberId " +
           "AND rs.isRead = false")
    int bulkUpdateIsReadByChatRoomIdAndMember(@Param("roomId") Long roomId, 
                                               @Param("memberId") Long memberId);


    // 4-A. 채팅방별 미읽은 메시지 수 조회
    @Query(value = """
    SELECT 
        rs.chat_room_id as roomId,
        COUNT(*) as unreadCount
    FROM read_status rs
    WHERE rs.chat_room_id IN :roomIds
    AND rs.member_id = :memberId
    AND rs.is_read = false
    GROUP BY rs.chat_room_id
    """, nativeQuery = true)
    List<UnreadCountProjection> findUnreadCountsByRoomIds(@Param("roomIds") List<Long> roomIds,
                                                          @Param("memberId") Long memberId);
}
