package com.lovecube.backend.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_statistics")
public class UserStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column
    private Integer todayVisitors;

    @Column
    private Integer totalVisitors;

    @Column
    private Integer newLikes;

    @Column
    private Integer totalLikes;

    @Column
    private Integer newMatches;

    @Column
    private Integer totalMatches;

    @Column
    private Integer completionRate;

    @Column
    private Boolean isVip;

    @Column
    private LocalDateTime vipExpireDate;

    @Column
    private Integer walletBalance;

    @Column
    private Integer unreadMessages;

    @Column
    private LocalDateTime lastStatisticsReset;

    @PrePersist
    protected void onCreate() {
        if (lastStatisticsReset == null) {
            lastStatisticsReset = LocalDateTime.now();
        }
        if (todayVisitors == null) todayVisitors = 0;
        if (totalVisitors == null) totalVisitors = 0;
        if (newLikes == null) newLikes = 0;
        if (totalLikes == null) totalLikes = 0;
        if (newMatches == null) newMatches = 0;
        if (totalMatches == null) totalMatches = 0;
        if (completionRate == null) completionRate = 0;
        if (isVip == null) isVip = false;
        if (walletBalance == null) walletBalance = 0;
        if (unreadMessages == null) unreadMessages = 0;
    }
} 