package com.lovecube.backend.models;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "chatmessages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;
    private Long receiverId;
    private String content;
    private Long timestamp;
    private boolean isRead = false;  // 是否已读
}
