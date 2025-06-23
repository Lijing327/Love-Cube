package com.lovecube.backend.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "chatmessages")
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

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        isRead = false;
    }
}
