package com.lovecube.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.id.factory.spi.GenerationTypeStrategy;

import java.util.Date;


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

    @Column(name = "userid")
    private Long userid; //用户ID

    @Column(unique = true, nullable = false, name = "openid")
    private String openid; // 微信用户的唯一标识

    @Column(name = "username")
    private String username;//用户名

    @Column(name = "password_hash")
    private String password_hash;//加密后的密码

    @Column(name = "email")
    private String email;//邮箱

    @Column(name = "phone_number")
    private String phone_number;//手机号

    @Column(name = "gender")
    private Integer gender;//性别

    @Column(name = "age")
    private Integer age;//年龄

    @Column(name = "birth_date")
    private Date birth_date;//出生日期

    @Column(name = "location")
    private String location;// 用户所在地

    @Column(name = "occupation")
    private String occupation;//工作

    @Column(name = "bio")
    private String bio;//个人简介

    @Column(name = "profile_photo")
    private String profile_photo;//头像url

    @Column(name = "height")
    private Integer height;

    @Column(name = "created_at")
    private Date created_at;//注册时间

    @Column(name = "updated_at")
    private Date updated_at;//最后更新时间

    @PrePersist
    protected void onCreate() {
        created_at = new Date();
        updated_at = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = new Date();
    }
}
