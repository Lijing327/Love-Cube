package com.lovecube.backend.repository;

import com.lovecube.backend.models.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 匹配记录 DAO
 */
@Repository
public interface MatchRecordRepository extends JpaRepository<MatchRecord, Long>
{
    List<MatchRecord> findByUserId(Long userId);

}
