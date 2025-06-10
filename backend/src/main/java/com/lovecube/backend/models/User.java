package com.lovecube.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author Admin
 * 创建一个 User 实体类，使用JPA注解来映射数据库中的 users 表
 */
@Entity
@Data
@Table(name = "users")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userid; //用户ID

    @Column(name = "username")
    private String username;//用户名

    @Column(name = "email")
    private String email;//邮箱

    @Column(name = "phone_number")
    private String phoneNumber;//手机号

    @Column(name = "password_hash")
    private String passwordHash;//加密后的密码

    @Column(name = "openid")
    private String openid; // 微信用户的唯一标识

    @Column(name = "profile_photo")
    private String profilePhoto;//头像url

    @Column(name = "bio")
    private String bio;//个人简介

    @Column(name = "age")
    private Integer age;//年龄

    @Column(name = "gender")
    private Integer gender;//性别

    @Column(name = "location")
    private String location;// 用户所在地

    @Column(name = "occupation")
    private String occupation;//工作

    @Column(name = "height")
    private Integer height;

    @Column(name = "birth_date")
    private LocalDateTime birthDate;//出生日期

    @Column(name = "created_at")
    private LocalDateTime createdAt;//注册时间

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;//最后更新时间

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
