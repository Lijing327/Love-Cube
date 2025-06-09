package com.lovecube.backend.services;

import com.alibaba.fastjson.JSONObject;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatService
{

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${wechat.appid}")
    private String appid;
    @Value("${wechat.secret}")
    private String secret;
    @Autowired
    private UserRepository userRepository; // 注入数据库访问层

    public Map<String, Object> login(String code)
    {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code);

        // 发送请求到微信API，获取openid
        String response = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.parseObject(response);
        String openid = jsonObject.getString("openid");

        Map<String, Object> result = new HashMap<>();
        if (openid != null) {
            // 查询数据库是否有该用户
            User user = userRepository.findByOpenid(openid);
            if (user != null) {
                // ✅ 生成 JWT
                String token = JwtUtil.generateToken(user.getOpenid());
                System.out.println("✅ 生成的 Token: " + token); // 打印检查
                result.put("userId", user.getUserid());
                result.put("token", token);
            } else {
                // 用户未注册
                result.put("status", "USER_NOT_REGISTERED");
                result.put("openid", openid);
            }
        } else {
            result.put("status", "ERROR");
            result.put("message", "微信登录失败");
        }
        return result;
    }

    private String generateToken(User user)
    {
        // 这里可以使用 JWT 生成 Token
        return "mockToken-" + user.getUserid();
    }
}