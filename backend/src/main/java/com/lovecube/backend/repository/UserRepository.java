package com.lovecube.backend.repository;

import com.lovecube.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Admin
 * 继承 JpaRepository，并添加一个自定义查询方法来排除当前用户。
 */
public interface UserRepository extends JpaRepository<User, Long>
{
    @Query("SELECT u FROM User u WHERE u.userid <> :userId")
    List<User> findAllExceptCurrentUser(@Param("userId") Long userId);

    User findByOpenid(String openid);

    boolean existsByOpenid(String openid);

    // 按年龄、性别、地区查询用户
    // **年龄范围查询，性别可选，地区可选**
    @Query("SELECT u FROM User u WHERE "
            + "(:minAge IS NULL OR u.age >= :minAge) "
            + "AND (:maxAge IS NULL OR u.age <= :maxAge) "
            + "AND (:gender IS NULL OR u.gender = :gender) "
            + "AND (:location IS NULL OR LOWER(u.location) = LOWER(:location))") // 确保大小写不敏感
    List<User> findByAgeBetweenAndGenderAndLocation(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("gender") Integer gender,
            @Param("location") String location
    );

}



