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
    public List<User> findMatchUsers(Long userId, Integer minAge, Integer maxAge, Integer gender, String location) {
        logger.info("开始查找匹配用户 - userId: {}, minAge: {}, maxAge: {}, gender: {}, location: {}", 
                   userId, minAge, maxAge, gender, location);

        // 参数验证
        if (userId == null) {
            logger.error("用户ID为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 获取当前用户
        Optional<User> currentUserOpt = userRepository.findById(userId);
        if (!currentUserOpt.isPresent()) {
            logger.error("用户不存在 - userId: {}", userId);
            throw new IllegalArgumentException("用户不存在");
        }
        User currentUser = currentUserOpt.get();
        logger.info("当前用户信息 - username: {}, age: {}, gender: {}", 
                   currentUser.getUsername(), currentUser.getAge(), currentUser.getGender());

        // 设置默认值
        minAge = minAge == null ? 18 : minAge;
        maxAge = maxAge == null ? 100 : maxAge;

        // 构建查询条件
        List<User> potentialMatches = new ArrayList<>();
        try {
            if (gender != null && location != null) {
                potentialMatches = userRepository.findByAgeBetweenAndGenderAndLocation(minAge, maxAge, gender, location);
                logger.info("使用完整条件查询 - 找到 {} 个用户", potentialMatches.size());
            } else if (gender != null) {
                potentialMatches = userRepository.findByAgeBetweenAndGender(minAge, maxAge, gender);
                logger.info("使用年龄和性别查询 - 找到 {} 个用户", potentialMatches.size());
            } else if (location != null) {
                potentialMatches = userRepository.findByAgeBetweenAndLocation(minAge, maxAge, location);
                logger.info("使用年龄和地区查询 - 找到 {} 个用户", potentialMatches.size());
            } else {
                potentialMatches = userRepository.findByAgeBetween(minAge, maxAge);
                logger.info("仅使用年龄查询 - 找到 {} 个用户", potentialMatches.size());
            }
        } catch (Exception e) {
            logger.error("数据库查询出错", e);
            throw new RuntimeException("查询用户失败: " + e.getMessage());
        }

        // 过滤掉当前用户
        potentialMatches = potentialMatches.stream()
                .filter(user -> !user.getUserid().equals(userId))
                .collect(Collectors.toList());
        logger.info("过滤掉当前用户后 - 剩余 {} 个用户", potentialMatches.size());

        // 获取已经匹配过的用户ID列表
        List<Long> matchedUserIds;
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
        score += Math.max(0, 1 - (ageDiff / 10.0)); // 每差10岁扣1分

        // 地理位置评分
        if (user1.getLocation() != null && user2.getLocation() != null) {
            if (user1.getLocation().equals(user2.getLocation())) {
                score += 1.0; // 同城加1分
            }
        }

        // 职业评分
        if (user1.getOccupation() != null && user2.getOccupation() != null) {
            if (user1.getOccupation().equals(user2.getOccupation())) {
                score += 0.5; // 同行业加0.5分
            }
        }

        // 标准化分数到0-1之间
        return Math.min(1.0, score / 2.5);
    }
}
