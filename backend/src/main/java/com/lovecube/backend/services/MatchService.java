package com.lovecube.backend.services;

import com.lovecube.backend.models.MatchRecord;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.MatchRecordRepository;
import com.lovecube.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchService
{
    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRecordRepository matchRecordRepository;

    @Transactional
    public List<User> findMatches(Long userId, Integer minAge, Integer maxAge, Integer gender, String location) {
        logger.info("开始查找匹配 - userId: {}, minAge: {}, maxAge: {}, gender: {}, location: {}", 
            userId, minAge, maxAge, gender, location);

        // 参数验证
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }

        if (minAge != null && maxAge != null && minAge > maxAge) {
            throw new IllegalArgumentException("最小年龄不能大于最大年龄");
        }

        // 获取当前用户
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 构建查询条件
        List<User> potentialMatches = userRepository.findAll().stream()
            .filter(user -> !user.getUserid().equals(userId))
            .filter(user -> minAge == null || user.getAge() >= minAge)
            .filter(user -> maxAge == null || user.getAge() <= maxAge)
            .filter(user -> gender == null || user.getGender().equals(gender))
            .filter(user -> location == null || user.getLocation().contains(location))
            .collect(Collectors.toList());

        logger.info("初步筛选 - 找到 {} 个潜在匹配", potentialMatches.size());

        // 获取已匹配用户列表
        final List<Long> matchedUserIds;
        try {
            matchedUserIds = matchRecordRepository.findByUserId(userId)
                    .stream()
                    .map(MatchRecord::getMatchedUserId)
                    .collect(Collectors.toList());
            logger.info("已匹配用户数量: {}", matchedUserIds.size());
        } catch (Exception e) {
            logger.error("获取已匹配用户失败", e);
            throw new RuntimeException("获取匹配记录失败: " + e.getMessage());
        }

        // 过滤掉已经匹配过的用户
        potentialMatches = potentialMatches.stream()
                .filter(user -> !matchedUserIds.contains(user.getUserid()))
                .collect(Collectors.toList());
        logger.info("过滤掉已匹配用户后 - 最终匹配用户数量: {}", potentialMatches.size());

        // 计算匹配分数并创建匹配记录
        List<MatchRecord> newMatchRecords = new ArrayList<>();
        try {
            newMatchRecords = potentialMatches.stream()
                    .map(user -> {
                        MatchRecord record = new MatchRecord();
                        record.setUserId(userId);
                        record.setMatchedUserId(user.getUserid());
                        record.setMatchScore(calculateMatchScore(currentUser, user));
                        return record;
                    })
                    .collect(Collectors.toList());

            // 保存匹配记录
            if (!newMatchRecords.isEmpty()) {
                matchRecordRepository.saveAll(newMatchRecords);
                logger.info("成功保存 {} 条匹配记录", newMatchRecords.size());
            }
        } catch (Exception e) {
            logger.error("保存匹配记录失败", e);
            throw new RuntimeException("保存匹配记录失败: " + e.getMessage());
        }

        return potentialMatches;
    }

    private double calculateMatchScore(User user1, User user2) {
        double score = 0.0;
        
        // 年龄差异评分（年龄差越小分数越高）
        int ageDiff = Math.abs(user1.getAge() - user2.getAge());
        score += Math.max(0, 10 - ageDiff) * 2; // 最高20分
        
        // 地理位置评分
        if (user1.getLocation() != null && user2.getLocation() != null) {
            if (user1.getLocation().equals(user2.getLocation())) {
                score += 30; // 同一地区加30分
            } else if (user1.getLocation().length() >= 2 && user2.getLocation().length() >= 2 &&
                     user1.getLocation().substring(0, 2).equals(user2.getLocation().substring(0, 2))) {
                score += 15; // 同一省份加15分
            }
        }
        
        // 职业评分
        if (user1.getOccupation() != null && user2.getOccupation() != null &&
            user1.getOccupation().equals(user2.getOccupation())) {
            score += 15; // 相同职业加15分
        }
        
        return score;
    }
}
