package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {
    UserStatistics findByUserId(Long userId);

    @Query("SELECT us FROM UserStatistics us WHERE us.userId = :userId AND us.lastStatisticsReset < :today")
    UserStatistics findByUserIdAndNeedsReset(@Param("userId") Long userId, @Param("today") LocalDateTime today);
} 