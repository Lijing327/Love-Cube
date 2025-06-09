package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String avatar;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String gender;

    @Column
    private String city;

    @Column
    private String province;

    @Column
    private String tag;

    @ElementCollection
    @CollectionTable(name = "user_tags", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column
    private Boolean hasHouse;

    @Column
    private Boolean hasCar;

    @Column
    private Integer annualIncome;

    @Column
    private String education;

    @Column
    private Boolean hasOverseasExperience;

    @Column
    private Boolean isSingle;

    @Column
    private LocalDateTime lastActiveTime;

    @Column
    private Boolean isNewcomer;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        lastActiveTime = LocalDateTime.now();
        if (isNewcomer == null) {
            isNewcomer = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
} 