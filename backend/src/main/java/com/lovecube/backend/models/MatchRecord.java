package com.lovecube.backend.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * 匹配记录实体类
 */
@Data
@Entity
@Table(name = "matchrecords")
public class MatchRecord
{

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "matched_user_id", nullable = false)
    private Long matchedUserId;

    @Column(name = "match_score")
    private Double matchScore;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
}
