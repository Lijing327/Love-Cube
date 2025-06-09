package com.lovecube.backend.repository;

import com.lovecube.backend.models.Greeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GreetingRepository extends JpaRepository<Greeting, Long>
{
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
}