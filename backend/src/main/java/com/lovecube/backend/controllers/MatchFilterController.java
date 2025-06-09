package com.lovecube.backend.controllers;

import com.lovecube.backend.models.MatchFilter;
import com.lovecube.backend.services.MatchFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/match")
public class MatchFilterController
{
    @Autowired
    private MatchFilterService matchFilterService;

    @PostMapping("/filters")
    public ResponseEntity<?> saveMatchFilters(@RequestBody MatchFilter matchFilter)
    {
        MatchFilter savedFilter = matchFilterService.saveMatchFilter(
                matchFilter.getUserId(), matchFilter.getAgeRange(), matchFilter.getGender(), matchFilter.getLocation()
        );
        return ResponseEntity.ok(savedFilter);
    }
}
