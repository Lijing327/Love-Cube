package com.lovecube.backend.services;

import com.lovecube.backend.dto.UserFilterDTO;
import com.lovecube.backend.entity.Banner;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.BannerRepository;
import com.lovecube.backend.repository.UserRepository;
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
    private UserRepository userRepository;

    public List<Banner> getBanners() {
        return bannerRepository.findByIsActiveTrueOrderBySortAsc();
    }

    public List<User> getRecommends() {
        return userRepository.findRandomUsers(10);
    }

    public List<User> getNewcomers() {
        return userRepository.findNewcomers(10);
    }

    public List<User> searchUsers(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(PageRequest.of(page, size))
                .getContent();
        }
        return userRepository.findAll(PageRequest.of(page, size))
                .getContent()
                .stream()
                .filter(user -> 
                    (user.getUsername() != null && user.getUsername().toLowerCase().contains(keyword.toLowerCase())) ||
                    (user.getLocation() != null && user.getLocation().toLowerCase().contains(keyword.toLowerCase())) ||
                    (user.getOccupation() != null && user.getOccupation().toLowerCase().contains(keyword.toLowerCase()))
                )
                .collect(Collectors.toList());
    }

    public List<User> filterUsers(UserFilterDTO filterDTO) {
        return userRepository.findByAgeBetweenAndGenderAndLocation(
            filterDTO.getAgeRange().get(0),
            filterDTO.getAgeRange().get(1),
            "男".equals(filterDTO.getGender()) ? 1 : 2,
            filterDTO.getRegion()
        );
    }
} 