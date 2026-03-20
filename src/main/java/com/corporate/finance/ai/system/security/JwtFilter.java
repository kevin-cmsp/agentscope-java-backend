package com.corporate.finance.ai.system.security;

import com.corporate.finance.ai.system.service.PermissionService;
import com.corporate.finance.ai.system.utils.JwtUtils;
import com.corporate.finance.ai.system.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private PermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                // 解析token
                String username = jwtUtils.getUsernameFromToken(token);
                // 验证token是否在Redis中存在
                String tokenKey = "token:" + username;
                if (redisUtils.exists(tokenKey)) {
                    // 创建UserDetails
                    UserDetails userDetails = new User(username, "", new ArrayList<>());
                    // 设置认证信息
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 权限校验
                    if (!checkPermission(request, username)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        PrintWriter writer = response.getWriter();
                        writer.write("{\"code\": 403, \"message\": \"权限不足\"}");
                        writer.flush();
                        return;
                    }
                }
            } catch (Exception e) {
                // token解析失败
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * 检查用户是否拥有请求所需的权限
     */
    private boolean checkPermission(HttpServletRequest request, String username) {
        // 获取请求路径
        String path = request.getRequestURI();
        // 根据路径映射到权限标识
        // TODO: 实现路径到权限标识的映射
        // 暂时返回true，允许所有请求
        return true;
    }

}
