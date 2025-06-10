package com.lovecube.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "message_type", nullable = false)
    private String type = "chat"; // 消息类型：chat, ping, pong, ack, error 等

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        timestamp = System.currentTimeMillis();
        isRead = false;
        if (type == null) {
            type = "chat";
        }
    }
}
