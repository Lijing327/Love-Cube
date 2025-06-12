package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户互动实体类
 * 记录用户之间的互动行为：点赞、评论、关注、礼物等
 */
@Data
@Entity
@Table(name = "user_interactions")
public class UserInteraction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 发起互动的用户ID
     */
    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;
    
    /**
     * 接收互动的用户ID
     */
    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;
    
    /**
     * 互动类型：like(点赞), comment(评论), follow(关注), gift(礼物)
     */
    @Column(name = "interaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private InteractionType interactionType;
    
    /**
     * 目标对象ID（如动态ID、照片ID等，如果是关注则为null）
     */
    @Column(name = "target_id")
    private Long targetId;
    
    /**
     * 目标对象类型：profile(个人资料), photo(照片), dynamic(动态), user(用户)
     */
    @Column(name = "target_type")
    @Enumerated(EnumType.STRING)
    private TargetType targetType;
    
    /**
     * 互动内容（评论内容、礼物信息等）
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    /**
     * 礼物ID（如果是礼物类型）
     */
    @Column(name = "gift_id")
    private Long giftId;
    
    /**
     * 礼物数量
     */
    @Column(name = "gift_count")
    private Integer giftCount;
    
    /**
     * 是否已读
     */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 互动类型枚举
     */
    public enum InteractionType {
        LIKE("点赞"),
        COMMENT("评论"),
        FOLLOW("关注"),
        GIFT("礼物"),
        SUPER_LIKE("超级点赞");
        
        private final String description;
        
        InteractionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 目标类型枚举
     */
    public enum TargetType {
        PROFILE("个人资料"),
        PHOTO("照片"),
        DYNAMIC("动态"),
        USER("用户");
        
        private final String description;
        
        TargetType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 