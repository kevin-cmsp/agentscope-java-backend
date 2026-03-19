package com.corporate.finance.ai.service;

import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 技能管理服务
 * 
 * 该服务负责管理Agent的技能（工具）集合，
 * 提供技能的注册、查询、卸载等功能。
 * 
 * 主要功能：
 * - 注册新的技能（工具）
 * - 根据名称获取技能
 * - 列出所有已注册的技能
 * - 卸载指定的技能
 * 
 * 技术实现：
 * - 使用HashMap存储技能集合
 * - Key: 技能名称
 * - Value: 技能信息（包含工具实例和方法列表）
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Service
public class SkillService {

    /**
     * 技能存储集合
     * Key: 技能名称
     * Value: 技能信息对象
     */
    private final Map<String, SkillInfo> skills = new HashMap<>();

    /**
     * 注册技能（工具）
     * 
     * 将新的技能添加到技能集合中，
     * 如果技能名称已存在，则会覆盖旧技能。
     * 
     * @param skillName 技能名称
     * @param toolInstance 工具实例对象
     */
    public void registerSkill(String skillName, Object toolInstance) {
        // 扫描工具实例中所有带有@Tool注解的方法
        List<MethodInfo> methods = new ArrayList<>();
        for (Method method : toolInstance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(io.agentscope.core.tool.Tool.class)) {
                io.agentscope.core.tool.Tool toolAnnotation = 
                    method.getAnnotation(io.agentscope.core.tool.Tool.class);
                methods.add(new MethodInfo(
                    toolAnnotation.name(),
                    toolAnnotation.description(),
                    method
                ));
            }
        }
        
        // 创建技能信息对象
        SkillInfo skillInfo = new SkillInfo(toolInstance, methods);
        
        // 存储到集合中
        skills.put(skillName, skillInfo);
    }

    /**
     * 根据名称获取技能信息
     * 
     * @param skillName 技能名称
     * @return 技能信息对象，如果不存在则返回null
     */
    public SkillInfo getSkill(String skillName) {
        return skills.get(skillName);
    }

    /**
     * 列出所有已注册的技能
     * 
     * 返回技能集合的副本，避免外部修改影响内部数据。
     * 
     * @return 技能名称与技能信息的映射集合
     */
    public Map<String, SkillInfo> listSkills() {
        return new HashMap<>(skills);
    }

    /**
     * 卸载指定的技能
     * 
     * 从技能集合中移除指定的技能。
     * 
     * @param skillName 技能名称
     */
    public void unregisterSkill(String skillName) {
        skills.remove(skillName);
    }

    /**
     * 技能信息类
     * 
     * 封装了技能的工具实例和方法列表。
     */
    public static class SkillInfo {
        private final Object toolInstance;
        private final List<MethodInfo> methods;

        public SkillInfo(Object toolInstance, List<MethodInfo> methods) {
            this.toolInstance = toolInstance;
            this.methods = methods;
        }

        public Object getToolInstance() {
            return toolInstance;
        }

        public List<MethodInfo> getMethods() {
            return methods;
        }
    }

    /**
     * 方法信息类
     * 
     * 封装了工具方法的名称、描述和Method对象。
     */
    public static class MethodInfo {
        private final String name;
        private final String description;
        private final Method method;

        public MethodInfo(String name, String description, Method method) {
            this.name = name;
            this.description = description;
            this.method = method;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Method getMethod() {
            return method;
        }
    }
}
