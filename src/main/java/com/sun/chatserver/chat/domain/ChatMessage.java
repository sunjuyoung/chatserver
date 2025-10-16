package com.sun.chatserver.chat.domain;

import com.sun.chatserver.chat.common.domain.BaseTimeEntity;
import com.sun.chatserver.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Table(
        name = "chat_messages",
        indexes = {
                @Index(name = "idx_message_chat_room_id", columnList = "chat_room_id"),
                @Index(name = "idx_message_sender_id", columnList = "sender_id"),
                @Index(name = "idx_message_created_at", columnList = "created_at"),
                @Index(name = "idx_message_room_time", columnList = "chat_room_id,created_at"),
        }
)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private MessageType type = MessageType.TEXT;

    @Column(columnDefinition = "TEXT")
    private String content;



}
