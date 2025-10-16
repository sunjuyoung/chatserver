package com.sun.chatserver.chat.repository;

import com.sun.chatserver.chat.domain.ChatRoom;
import com.sun.chatserver.chat.domain.ChatRoomType;
import com.sun.chatserver.chat.repository.projection.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    //findByIdWithMembers @EntityGraph로 대체
    @EntityGraph(attributePaths = {"members", "members.member"})
    ChatRoom findWithMembersById(Long id);


    //타입이 GROUP and isActive가 true이고  createdAt 최신순으로 조회
    List<ChatRoom> findByTypeAndIsActiveTrueOrderByCreatedAtDesc(ChatRoomType type);

    @Query(value = " SELECT cr " +
            " FROM ChatRoom cr " +
            " ORDER BY cr.createdAt DESC LIMIT 5")
    List<ChatRoom> findAllChatRoom();


    @Query(value = " SELECT cr " +
            " FROM ChatRoom cr " +
            " WHERE cr.id < :lastId " +
            " ORDER BY cr.createdAt DESC LIMIT 5")
    List<ChatRoom> findAllChatRoom(@Param("lastId") Long lastId);


    // 1. 기본 채팅방 정보 조회
    @Query(value = """
    SELECT 
        cr.id as roomId,
        cr.name as roomName,
        cr.created_at as createdAt,
        cr.updated_at as updatedAt
    FROM chat_rooms cr
    INNER JOIN chat_room_members crm ON cr.id = crm.chat_room_id
    WHERE crm.member_id = :memberId
    AND crm.left_at IS NULL
    AND cr.is_active = true
    """, nativeQuery = true)
    List<ChatRoomBasicProjection> findBasicChatRoomsByMemberId(@Param("memberId") Long memberId);

    // 2. 채팅방별 멤버 이메일 조회
    @Query(value = """

    SELECT
        crm.chat_room_id as roomId,
        COUNT(m.id) as memberCount
    FROM chat_room_members crm
    INNER JOIN member m ON crm.member_id = m.id
    WHERE crm.chat_room_id IN :roomIds
    AND crm.left_at IS NULL
    GROUP BY crm.chat_room_id
    """, nativeQuery = true)
    List<ChatRoomMembersProjection> findMembersByRoomIds(@Param("roomIds") List<Long> roomIds);

    // 3. 채팅방별 최근 메시지 조회
    @Query(value = """
    SELECT 
        cm1.chat_room_id as roomId,
        cm1.id as lastMessageId,
        m.name as lastMessageSenderName,
        cm1.content as lastMessageContent,
        cm1.created_at as lastMessageCreatedAt
    FROM chat_messages cm1
    INNER JOIN member m ON cm1.sender_id = m.id
    INNER JOIN (
        SELECT chat_room_id, MAX(id) as max_id
        FROM chat_messages
        WHERE chat_room_id IN :roomIds
        GROUP BY chat_room_id
    ) cm2 ON cm1.chat_room_id = cm2.chat_room_id AND cm1.id = cm2.max_id
    """, nativeQuery = true)
    List<LastMessageProjection> findLastMessagesByRoomIds(@Param("roomIds") List<Long> roomIds);













    /**
     * 특정 사용자가 속한 채팅방 목록 조회
     * - 채팅방 멤버 이메일 목록
     * - 가장 최근 메시지 정보
     * - 미읽은 메시지 수
     *
     * @param memberId 조회할 사용자 ID
     *
     *
     *                 [
     *     {
     *         "roomId": "1",
     *         "roomName": "testRoom",
     *         "chatRoomMembers": [
     *             "test@test.com",
     *             "test1@test.com"
     *         ],
     *         "lastMessage": {
     *             "id": "16",
     *             "senderName": "aaa",
     *             "content": "몰라?",
     *             "createdAt": "2025-10-15T14:52:59"
     *         },
     *         "unreadCount": 14,
     *         "createdAt": "2025-10-11T14:30:43",
     *         "updatedAt": "2025-10-11T14:30:43"
     *     },
     *                 ]
     */
    @Query(value = """

            SELECT 
            cr.id as roomId,
            cr.name as roomName,
            cr.created_at as createdAt,
            cr.updated_at as updatedAt,
            (
                SELECT GROUP_CONCAT(m.email ORDER BY m.name SEPARATOR ',')
                FROM chat_room_members crm2
                INNER JOIN member m ON crm2.member_id = m.id
                WHERE crm2.chat_room_id = cr.id
                AND crm2.left_at IS NULL
            ) as chatRoomMembers,
            (
                SELECT cm.id 
                FROM chat_messages cm 
                WHERE cm.chat_room_id = cr.id 
                ORDER BY cm.created_at DESC 
                LIMIT 1
            ) as lastMessageId,
            (
                SELECT m.name 
                FROM chat_messages cm 
                INNER JOIN member m ON cm.sender_id = m.id
                WHERE cm.chat_room_id = cr.id 
                ORDER BY cm.created_at DESC 
                LIMIT 1
            ) as lastMessageSenderName,
            (
                SELECT cm.content 
                FROM chat_messages cm 
                WHERE cm.chat_room_id = cr.id 
                ORDER BY cm.created_at DESC 
                LIMIT 1
            ) as lastMessageContent,
            (
                SELECT cm.created_at 
                FROM chat_messages cm 
                WHERE cm.chat_room_id = cr.id 
                ORDER BY cm.created_at DESC 
                LIMIT 1
            ) as lastMessageCreatedAt,
            COALESCE(
                (
                    SELECT COUNT(*) 
                    FROM chat_messages cm2
                    WHERE cm2.chat_room_id = cr.id
                    AND cm2.id > COALESCE(crm.last_read_message_id, 0)
                ),
                0
            ) as unreadCount
        FROM chat_rooms cr
        INNER JOIN chat_room_members crm ON cr.id = crm.chat_room_id
        WHERE crm.member_id = :memberId
        AND crm.left_at IS NULL
        AND cr.is_active = true
        ORDER BY 
            CASE 
                WHEN (SELECT cm.created_at FROM chat_messages cm WHERE cm.chat_room_id = cr.id ORDER BY cm.created_at DESC LIMIT 1) IS NOT NULL
                THEN (SELECT cm.created_at FROM chat_messages cm WHERE cm.chat_room_id = cr.id ORDER BY cm.created_at DESC LIMIT 1)
                ELSE cr.updated_at
            END DESC
        """, nativeQuery = true)
    List<ChatRoomListProjection> findChatRoomListByMemberId(@Param("memberId") Long memberId);




    
    /**
     * 전체 채팅방 목록 조회
     * - 채팅방 멤버 이메일 목록
     * - 가장 최근 메시지 정보
     * - unreadCount는 필요없음
     *
     */
    }