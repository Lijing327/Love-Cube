package com.lovecube.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserFilterDTO {
    private List<Integer> ageRange;
    private String gender;
    private String region;
    private List<String> tags;
} 