package com.lovecube.backend.services;

import com.lovecube.backend.models.MatchRecord;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.MatchRecordRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRecordRepository matchRecordRepository;

    public List<User> findMatchUsers(Long userId, Integer minAge, Integer maxAge, Integer gender, String location) {
        // **确保默认年龄范围**
        if (minAge == null) minAge = 0;
        if (maxAge == null) maxAge = 100;

        // 根据筛选条件查询用户
        List<User> users = userRepository.findByAgeBetweenAndGenderAndLocation(minAge, maxAge, gender, location);

        System.out.println("🔍 查询参数: minAge=" + minAge + ", maxAge=" + maxAge + ", gender=" + gender + ", location=" + location);
        System.out.println("✅ 返回用户数量: " + users.size());

        // **计算匹配分数（简单实现，后续可优化）**
        List<MatchRecord> matchRecords = users.stream().map(user -> {
            MatchRecord record = new MatchRecord();
            record.setUserId(userId);
            record.setMatchedUserId(user.getUserid());
            record.setMatchScore(Math.random());
            return record;
        }).collect(Collectors.toList());


        // 存储匹配记录
        matchRecordRepository.saveAll(matchRecords);
        return users;
    }
}
