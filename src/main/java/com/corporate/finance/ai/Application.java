package com.corporate.finance.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AgentScope Java AI开发平台应用入口类
 * 
 * 该类是Spring Boot应用的主入口，负责启动整个应用程序。
 * 使用@SpringBootApplication注解启用自动配置和组件扫描。
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@SpringBootApplication
public class Application {
    
    /**
     * 应用程序主入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
