package com.lovecube.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatPartnerDTO {
    private Long id;
    private String username;
    private String avatar;
    private String lastMessage; // 最近一条消息
    private String time; // 消息时间

    // 这里添加一个匹配 Object[] 参数的构造函数
    public ChatPartnerDTO(Object[] obj) {
        this.id = ((Number) obj[0]).longValue(); // 转换 ID
        this.username = (String) obj[1];
        this.avatar = (String) obj[2];
        this.lastMessage =(String) obj[3];
        this.time = (String) obj[4];

    }
}


