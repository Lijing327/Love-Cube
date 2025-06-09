package com.lovecube.backend.services;

import com.lovecube.backend.models.MatchFilter;
import com.lovecube.backend.repository.MatchFilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MatchFilterService
{
    @Autowired
    private MatchFilterRepository matchFilterRepository;

    public MatchFilter saveMatchFilter(Long userId, String ageRange, Integer gender, String location)
    {
        MatchFilter filter = new MatchFilter();
        filter.setUserId(userId);
        filter.setAgeRange(ageRange);
        filter.setGender(gender);
        filter.setLocation(location);
        return matchFilterRepository.save(filter);
    }
}
