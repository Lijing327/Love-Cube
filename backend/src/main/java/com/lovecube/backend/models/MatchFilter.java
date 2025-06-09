package com.lovecube.backend.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * 匹配条件
 */
@Data
@Entity
@Table(name = "matchfilters")
public class MatchFilter
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "age_range")
    private String ageRange;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "location")
    private String location;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
}
