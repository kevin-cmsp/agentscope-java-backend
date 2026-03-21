package com.corporate.finance.ai.system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private static final String SECRET_KEY = "your-secret-key-for-jwt-token-generation-and-validation";
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24 小时

    private static final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成 JWT token
     */
    public String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);

        // 如果 claims 中包含 username，设置为 subject
        String subject = null;
        if (claims.containsKey("username")) {
            subject = (String) claims.get("username");
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)  // 设置 subject 为用户名
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 JWT token
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        // 优先获取 subject（用户名），如果没有则从 username 字段获取
        String username = claims.getSubject();
        if (username == null || username.isEmpty()) {
            username = claims.get("username", String.class);
        }
        return username;
    }

    /**
     * 验证 token 是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }

}
