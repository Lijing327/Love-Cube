package com.lovecube.backend.services;

import com.lovecube.backend.dto.UserSimilarityDTO;
import com.lovecube.backend.exception.UserNotFoundException;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.SimilarityCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Admin
 * 我们实现推荐逻辑，包括获取当前用户、计算相似度、排序并返回前N个推荐用户。
 */
@Service
public class UserService
{
    @Autowired
    private UserRepository userRepository;

    public List<UserSimilarityDTO> getRecommendedUsers(Long userId, int limit) throws UserNotFoundException
    {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        List<User> otherUsers = userRepository.findAllExceptCurrentUser(userId);

        return otherUsers.stream()
                .map(user ->
                    {
                        UserSimilarityDTO dto = new UserSimilarityDTO();
                        dto.setUsername(user.getUsername());
                        dto.setAge(user.getAge());
                        dto.setLocation(user.getLocation());
                        dto.setOccupation(user.getOccupation());
                        dto.setSimilarityScore(SimilarityCalculator.calculateSimilarity(currentUser, user));
                        return dto;
                    })
                .sorted(Comparator.comparingDouble(UserSimilarityDTO::getSimilarityScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public boolean isUserRegistered(String openid)
    {
        return userRepository.existsByOpenid(openid);
    }
}
