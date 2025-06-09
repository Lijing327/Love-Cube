package com.lovecube.backend.services;

import com.lovecube.backend.models.Greeting;
import com.lovecube.backend.repository.GreetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GreetingService
{

    @Autowired
    private GreetingRepository greetingRepository;

    public String sendGreeting(Long senderId, Long receiverId)
    {
        if (greetingRepository.existsBySenderIdAndReceiverId(senderId, receiverId)) {
            return "您已经向该用户打过招呼";
        }

        Greeting greeting = new Greeting();
        greeting.setSenderId(senderId);
        greeting.setReceiverId(receiverId);
        greetingRepository.save(greeting);

        return "打招呼成功";
    }
}
