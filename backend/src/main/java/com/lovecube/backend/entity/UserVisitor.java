package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户访客实体类
 * 记录用户资料被访问的记录
 */
@Data
@Entity
@Table(name = "user_visitors")
public class UserVisitor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 访客用户ID
     */
    @Column(name = "visitor_user_id", nullable = false)
    private Long visitorUserId;
    
    /**
     * 被访问的用户ID
     */
    @Column(name = "visited_user_id", nullable = false)
    private Long visitedUserId;
    
    /**
     * 访问类型：profile(查看资料), photo(查看照片), detail(详细查看)
     */
    @Column(name = "visit_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VisitType visitType;
    
    /**
     * 访问来源：search(搜索), recommend(推荐), match(匹配), link(链接)
     */
    @Column(name = "visit_source")
    @Enumerated(EnumType.STRING)
    private VisitSource visitSource;
    
    /**
     * 访问时长（秒）
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    /**
     * 是否是新访客（首次访问）
     */
    @Column(name = "is_new_visitor", nullable = false)
    private Boolean isNewVisitor = true;
    
    /**
     * 访问IP地址
     */
    @Column(name = "ip_address")
    private String ipAddress;
    
    /**
     * 设备信息
     */
    @Column(name = "device_info")
    private String deviceInfo;
    
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
        if (isNewVisitor == null) {
            isNewVisitor = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 访问类型枚举
     */
    public enum VisitType {
        PROFILE("查看资料"),
        PHOTO("查看照片"),
        DETAIL("详细查看"),
        QUICK_VIEW("快速浏览");
        
        private final String description;
        
        VisitType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 访问来源枚举
     */
    public enum VisitSource {
        SEARCH("搜索"),
        RECOMMEND("推荐"),
        MATCH("匹配"),
        LINK("链接"),
        DISCOVER("发现");
        
        private final String description;
        
        VisitSource(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 