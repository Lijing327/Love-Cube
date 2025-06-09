package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    @Query("SELECT u FROM UserProfile u WHERE u.isNewcomer = true ORDER BY u.createTime DESC")
    List<UserProfile> findNewcomers();

    @Query("SELECT u FROM UserProfile u WHERE " +
           "(:gender IS NULL OR u.gender = :gender) AND " +
           "(:minAge IS NULL OR u.age >= :minAge) AND " +
           "(:maxAge IS NULL OR u.age <= :maxAge) AND " +
           "(:region IS NULL OR u.city LIKE CONCAT(:region, '%'))")
    List<UserProfile> findByFilters(
        @Param("gender") String gender,
        @Param("minAge") Integer minAge,
        @Param("maxAge") Integer maxAge,
        @Param("region") String region
    );

    @Query(value = "SELECT * FROM user_profiles ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<UserProfile> findRandomUsers(@Param("limit") int limit);

    @Query("SELECT u FROM UserProfile u WHERE " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.tag) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "u.city LIKE CONCAT('%', :keyword, '%') OR " +
           "u.province LIKE CONCAT('%', :keyword, '%')")
    List<UserProfile> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
} 