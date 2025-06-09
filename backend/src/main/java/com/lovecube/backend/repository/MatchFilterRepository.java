package com.lovecube.backend.repository;

import com.lovecube.backend.models.MatchFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchFilterRepository extends JpaRepository<MatchFilter, Long>
{
    Optional<MatchFilter> findByUserId(Long userId);
}