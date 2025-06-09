package com.lovecube.backend.services;

import com.lovecube.backend.dto.UserFilterDTO;
import com.lovecube.backend.entity.Banner;
import com.lovecube.backend.entity.UserProfile;
import com.lovecube.backend.repository.BannerRepository;
import com.lovecube.backend.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HomeService {

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    public List<Banner> getBanners() {
        return bannerRepository.findByIsActiveTrueOrderBySortAsc();
    }

    public List<UserProfile> getRecommends() {
        return userProfileRepository.findRandomUsers(10);
    }

    public List<UserProfile> getNewcomers() {
        return userProfileRepository.findNewcomers();
    }

    public List<UserProfile> searchUsers(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userProfileRepository.findAll(PageRequest.of(page, size))
                .getContent();
        }
        return userProfileRepository.searchByKeyword(keyword, PageRequest.of(page, size));
    }

    public List<UserProfile> filterUsers(UserFilterDTO filterDTO) {
        String gender = filterDTO.getGender();
        Integer minAge = filterDTO.getAgeRange().get(0);
        Integer maxAge = filterDTO.getAgeRange().get(1);
        String region = filterDTO.getRegion();

        List<UserProfile> filteredUsers = userProfileRepository.findByFilters(
            gender, minAge, maxAge, region
        );

        // 如果有标签筛选，进行二次过滤
        if (filterDTO.getTags() != null && !filterDTO.getTags().isEmpty()) {
            filteredUsers = filteredUsers.stream()
                .filter(user -> {
                    if (user.getTags() == null) return false;
                    return user.getTags().containsAll(filterDTO.getTags());
                })
                .collect(Collectors.toList());
        }

        return filteredUsers;
    }
} 