package com.lovecube.backend.repository;

import com.lovecube.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Admin
 * 继承 JpaRepository，并添加一个自定义查询方法来排除当前用户。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
    @Query("SELECT u FROM User u WHERE u.userid != :userId")
    List<User> findAllExceptCurrentUser(@Param("userId") Long userId);

    User findByOpenid(String openid);

    boolean existsByOpenid(String openid);

    // 根据年龄范围、性别和地区查询
    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge AND u.gender = :gender AND u.location = :location")
    List<User> findByAgeBetweenAndGenderAndLocation(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("gender") Integer gender,
            @Param("location") String location);

    // 根据年龄范围和性别查询
    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge AND u.gender = :gender")
    List<User> findByAgeBetweenAndGender(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("gender") Integer gender);

    // 根据年龄范围和地区查询
    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge AND u.location = :location")
    List<User> findByAgeBetweenAndLocation(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("location") String location);

    // 仅根据年龄范围查询
    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge")
    List<User> findByAgeBetween(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge);

    @Query(value = "SELECT * FROM users ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<User> findRandomUsers(@Param("limit") int limit);

    @Query(value = "SELECT * FROM users ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<User> findNewcomers(@Param("limit") int limit);
}



