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
import java.util.HashSet;
import java.util.Set;

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
        // 添加日志，查看请求路径和 token
        String path = request.getRequestURI();
        String authorizationHeader = request.getHeader("Authorization");
        
        System.out.println("=== JwtFilter 开始处理请求 ===");
        System.out.println("请求路径：" + path);
        System.out.println("请求方法：" + request.getMethod());
        System.out.println("Authorization Header: " + (authorizationHeader != null ? "存在" : "不存在"));
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            System.out.println("Token 值：" + authorizationHeader.substring(0, Math.min(50, authorizationHeader.length() - 7)) + "...");
        }
        
        if (path.startsWith("/api/auth/login") || 
            path.startsWith("/api/auth/logout") || 
            path.startsWith("/api/auth/captcha")) {
            System.out.println("=== 放行：无需认证的接口 ===");
            chain.doFilter(request, response);
            return;
        }
        
        String token = authorizationHeader;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            System.out.println("=== 提取 Token 成功：" + token.substring(0, Math.min(30, token.length())) + "...");
            try {
                String username = jwtUtils.getUsernameFromToken(token);
                System.out.println("=== 解析用户名成功：" + username);
                String tokenKey = "token:" + username;
                if (redisUtils.exists(tokenKey)) {
                    System.out.println("=== Token 在 Redis 中存在，验证通过");
                    UserDetails userDetails = new User(username, "", new ArrayList<>());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    if (!checkPermission(request, username)) {
                        System.out.println("=== 权限检查失败");
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        PrintWriter writer = response.getWriter();
                        writer.write("{\"code\": 403, \"message\": \"权限不足\"}");
                        writer.flush();
                        return;
                    }
                } else {
                    System.out.println("=== Token 在 Redis 中不存在，已过期或无效");
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    PrintWriter writer = response.getWriter();
                    writer.write("{\"code\": 401, \"message\": \"token 已过期或无效\"}");
                    writer.flush();
                    return;
                }
            } catch (Exception e) {
                System.out.println("=== Token 解析失败：" + e.getMessage());
                e.printStackTrace();
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                PrintWriter writer = response.getWriter();
                writer.write("{\"code\": 401, \"message\": \"token 无效：" + e.getMessage() + "\"}");
                writer.flush();
                return;
            }
        } else {
            System.out.println("=== 未提供 Token");
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\": 401, \"message\": \"未提供 token\"}");
            writer.flush();
            return;
        }
        System.out.println("=== JwtFilter 处理完成，放行请求 ===");
        chain.doFilter(request, response);
    }

    private boolean checkPermission(HttpServletRequest request, String username) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        if ("admin".equals(username)) {
            return true;
        }
        
        Set<String> permissions = permissionService.getUserPermissions(username);
        
        if (permissions.isEmpty()) {
            return false;
        }
        
        return true;
    }

}
