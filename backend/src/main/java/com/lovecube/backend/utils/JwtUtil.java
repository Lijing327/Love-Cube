package com.lovecube.backend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

public class JwtUtil
{

    // **固定密钥**（至少 32 字节，否则会报错）
    private static final String SECRET_KEY_BASE64 = "TQkhg0lChQ5kx2i/f9FMtjyNSIbjJ+mwiF/GUxQsi7o=";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY_BASE64));

    // 10 天有效期
    private static final long EXPIRATION_TIME = 864_000_000;

    /**
     * 生成 JWT Token
     */
    public static String generateToken(String openid)
    {
        return Jwts.builder()
                .setSubject(openid)
                .setIssuedAt(new Date())  // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 10天后过期
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // 签名算法用 HS256
                .compact();
    }


    /**
     * ✅ 验证 Token 是否有效
     */
    public static boolean validateToken(String token)
    {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7); // 移除 "Bearer " 前缀
            }

            // ✅ 适配 `jjwt 0.12.x`，使用 `.parser()` 而不是 `.parserBuilder()`
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(SECRET_KEY)  // 使用新的 `.verifyWith()` 方法
                    .build()
                    .parseSignedClaims(token);

            return true; // 解析成功，说明 Token 有效
        } catch (JwtException e) {
            System.err.println("❌ Token 验证失败: " + e.getMessage());
            return false;
        }
    }


    /**
     * 解析 Token 获取 openid
     */
    public static String getOpenIdFromToken(String token)
    {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7); // 移除 "Bearer "
            }

            // ✅ `jjwt 0.12.6` 版本的新解析方式
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(SECRET_KEY)  // 直接使用 `verifyWith`
                    .build()
                    .parseSignedClaims(token);

            return claimsJws.getPayload().getSubject(); // 获取 openid
        } catch (JwtException e) {
            System.err.println("❌ Token 解析失败: " + e.getMessage());
            return null;
        }
    }


}
