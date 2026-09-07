package com.tianji.agent.config.tool;

import com.tianji.agent.model.dto.*;
import com.tianji.agent.model.vo.*;
import com.tianji.agent.service.*;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统业务工具类
 * <p>
 * 将业务系统的全部 CRUD 能力包装为 AI Agent 可调用的 Tool，
 * 让 AI 助手能全面操作系统内的 Agent、工具、知识库、用户、数据看板、日志等业务数据。
 * 除配置管理（Setup）仅保留查询外，其余模块均提供完整的增删改查能力。
 * 所有工具方法集中管理，方便后续增删改。
 * </p>
 */
@Component("systemBusinessTool")
public class SystemBusinessTool {

    @Autowired
    private AgentService agentService;

    @Autowired
    private ToolsService toolsService;

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private SetupService setupService;

    @Autowired
    private UserService userService;

    @Autowired
    private DataService dataService;

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private OperationLogService operationLogService;

    // ==================== Agent 智能体相关 ====================

    @Tool("查询系统中所有的 Agent 智能体列表，支持按关键词、类型、状态筛选")
    public String queryAgentList(
            @P("搜索关键词，可选") String keyword,
            @P("智能体类型，可选") String type,
            @P("状态：active-启用，inactive-禁用，可选") String status) {
        PageVO<AgentVO> page = agentService.queryPage(1, 50, blankToNull(keyword), blankToNull(type), blankToNull(status));
        List<AgentVO> list = page.getList();
        if (list == null || list.isEmpty()) {
            return "系统中暂无 Agent 智能体。";
        }
        return list.stream()
                .map(a -> String.format("- ID:%s | 名称:%s | 类型:%s | 状态:%s | Token用量:%d | 描述:%s",
                        a.getId(), a.getName(), a.getType(), a.getStatus(),
                        a.getTokens() != null ? a.getTokens() : 0, a.getDescription()))
                .collect(Collectors.joining("\n", "系统 Agent 列表（共" + list.size() + "个）：\n", ""));
    }

    @Tool("根据 ID 查询 Agent 智能体的详细信息")
    public String queryAgentById(@P("Agent ID") String id) {
        AgentVO agent = agentService.queryById(Long.valueOf(id));
        if (agent == null) {
            return "未找到 ID 为 " + id + " 的 Agent。";
        }
        return String.format(
                "Agent 详情：\n- ID：%s\n- 名称：%s\n- 类型：%s\n- 状态：%s\n- 描述：%s\n- Token用量：%d\n- 关联配置ID：%s\n- 关联知识库ID：%s",
                agent.getId(), agent.getName(), agent.getType(), agent.getStatus(),
                agent.getDescription(), agent.getTokens() != null ? agent.getTokens() : 0,
                agent.getSetupId(), agent.getKnowledgeIds());
    }

    @Tool("新增一个 Agent 智能体")
    public String createAgent(
            @P("智能体名称，必填") String name,
            @P("智能体类型，必填，如 chat/task/workflow") String type,
            @P("智能体描述，可选") String description,
            @P("关联配置ID，可选") String setupId,
            @P("关联知识库ID列表，多个用逗号分隔，可选，如 1,2,3") String knowledgeIds,
            @P("Token用量，可选，默认为0") String tokens) {
        AgentDTO dto = new AgentDTO();
        dto.setName(name);
        dto.setType(type);
        dto.setDescription(description);
        dto.setSetupId(parseLong(setupId));
        dto.setKnowledgeIds(parseLongList(knowledgeIds));
        dto.setTokens(parseLong(tokens) != null ? parseLong(tokens) : 0L);

        AgentVO result = agentService.create(dto);
        return "Agent 智能体创建成功！\n" + formatAgentDetail(result);
    }

    @Tool("修改一个 Agent 智能体")
    public String updateAgent(
            @P("Agent ID，必填") String id,
            @P("智能体名称，必填") String name,
            @P("智能体类型，必填，如 chat/task/workflow") String type,
            @P("智能体描述，可选") String description,
            @P("关联配置ID，可选") String setupId,
            @P("关联知识库ID列表，多个用逗号分隔，可选，如 1,2,3") String knowledgeIds,
            @P("Token用量，可选") String tokens) {
        AgentDTO dto = new AgentDTO();
        dto.setName(name);
        dto.setType(type);
        dto.setDescription(description);
        dto.setSetupId(parseLong(setupId));
        dto.setKnowledgeIds(parseLongList(knowledgeIds));
        dto.setTokens(parseLong(tokens));

        AgentVO result = agentService.update(Long.valueOf(id), dto);
        return "Agent 智能体修改成功！\n" + formatAgentDetail(result);
    }

    @Tool("删除一个 Agent 智能体")
    public String deleteAgent(@P("Agent ID，必填") String id) {
        agentService.delete(Long.valueOf(id));
        return "Agent 智能体（ID: " + id + "）已成功删除。";
    }

    @Tool("切换 Agent 智能体的启用/禁用状态")
    public String toggleAgentStatus(
            @P("Agent ID，必填") String id,
            @P("目标状态：active-启用，inactive-禁用") String status) {
        StatusDTO dto = new StatusDTO();
        dto.setStatus(status);
        String newStatus = agentService.toggleStatus(Long.valueOf(id), dto);
        return "Agent 智能体（ID: " + id + "）状态已切换为：" + newStatus + "。";
    }

    // ==================== Tools 工具管理相关 ====================

    @Tool("查询系统中所有的工具列表，支持按关键词、类型、状态筛选")
    public String queryToolsList(
            @P("搜索关键词，可选") String keyword,
            @P("工具类型：api/function/builtin，可选") String type,
            @P("状态：active-启用，inactive-禁用，可选") String status) {
        PageVO<ToolsVO> page = toolsService.queryPage(1, 50, blankToNull(keyword), blankToNull(type), blankToNull(status));
        List<ToolsVO> list = page.getList();
        if (list == null || list.isEmpty()) {
            return "系统中暂无工具。";
        }
        return list.stream()
                .map(t -> String.format("- ID:%s | 名称:%s | 类型:%s | 状态:%s | 描述:%s",
                        t.getId(), t.getName(), t.getType(), t.getStatus(), t.getDescription()))
                .collect(Collectors.joining("\n", "系统工具列表（共" + list.size() + "个）：\n", ""));
    }

    @Tool("根据 ID 查询工具的详细信息")
    public String queryToolsById(@P("工具 ID") String id) {
        ToolsVO tools = toolsService.queryById(Long.valueOf(id));
        if (tools == null) {
            return "未找到 ID 为 " + id + " 的工具。";
        }
        return String.format(
                "工具详情：\n- ID：%s\n- 名称：%s\n- 类型：%s\n- 状态：%s\n- 描述：%s\n- 接口地址：%s",
                tools.getId(), tools.getName(), tools.getType(), tools.getStatus(),
                tools.getDescription(), tools.getEndpoint() != null ? tools.getEndpoint() : "无");
    }

    @Tool("新增一个工具")
    public String createTool(
            @P("工具名称，必填") String name,
            @P("工具类型，必填：api/function/builtin") String type,
            @P("工具描述，可选") String description,
            @P("工具代码，可选") String code,
            @P("接口地址，可选") String endpoint) {
        ToolsDTO dto = new ToolsDTO();
        dto.setName(name);
        dto.setType(type);
        dto.setDescription(description);
        dto.setCode(code);
        dto.setEndpoint(endpoint);

        ToolsVO result = toolsService.create(dto);
        return "工具创建成功！\n- ID：" + result.getId() + "\n- 名称：" + result.getName()
                + "\n- 类型：" + result.getType() + "\n- 状态：" + result.getStatus();
    }

    @Tool("修改一个工具")
    public String updateTool(
            @P("工具 ID，必填") String id,
            @P("工具名称，必填") String name,
            @P("工具类型，必填：api/function/builtin") String type,
            @P("工具描述，可选") String description,
            @P("工具代码，可选") String code,
            @P("接口地址，可选") String endpoint) {
        ToolsDTO dto = new ToolsDTO();
        dto.setName(name);
        dto.setType(type);
        dto.setDescription(description);
        dto.setCode(code);
        dto.setEndpoint(endpoint);

        ToolsVO result = toolsService.update(Long.valueOf(id), dto);
        return "工具修改成功！\n- ID：" + result.getId() + "\n- 名称：" + result.getName()
                + "\n- 类型：" + result.getType() + "\n- 状态：" + result.getStatus();
    }

    @Tool("删除一个工具")
    public String deleteTool(@P("工具 ID，必填") String id) {
        toolsService.delete(Long.valueOf(id));
        return "工具（ID: " + id + "）已成功删除。";
    }

    @Tool("切换工具的启用/禁用状态")
    public String toggleToolStatus(
            @P("工具 ID，必填") String id,
            @P("目标状态：active-启用，inactive-禁用") String status) {
        StatusDTO dto = new StatusDTO();
        dto.setStatus(status);
        String newStatus = toolsService.toggleStatus(Long.valueOf(id), dto);
        return "工具（ID: " + id + "）状态已切换为：" + newStatus + "。";
    }

    // ==================== 知识库相关 ====================

    @Tool("查询系统中所有的知识库列表，支持按关键词、类型筛选")
    public String queryKnowledgeList(
            @P("搜索关键词，可选") String keyword,
            @P("知识库类型：document/qa/web，可选") String type) {
        PageVO<KnowledgeVO> page = knowledgeService.queryPage(1, 50, blankToNull(keyword), blankToNull(type));
        List<KnowledgeVO> list = page.getList();
        if (list == null || list.isEmpty()) {
            return "系统中暂无知识库。";
        }
        return list.stream()
                .map(k -> String.format("- ID:%s | 名称:%s | 类型:%s | 文档数:%d | 状态:%s | 描述:%s",
                        k.getId(), k.getName(), k.getType(), k.getDocumentCount(), k.getStatus(), k.getDescription()))
                .collect(Collectors.joining("\n", "系统知识库列表（共" + list.size() + "个）：\n", ""));
    }

    @Tool("根据 ID 查询知识库的详细信息")
    public String queryKnowledgeById(@P("知识库 ID") String id) {
        KnowledgeVO knowledge = knowledgeService.queryById(Long.valueOf(id));
        if (knowledge == null) {
            return "未找到 ID 为 " + id + " 的知识库。";
        }
        return String.format(
                "知识库详情：\n- ID：%s\n- 名称：%s\n- 类型：%s\n- 文档数量：%d\n- 状态：%s\n- 描述：%s\n- 存储路径：%s",
                knowledge.getId(), knowledge.getName(), knowledge.getType(),
                knowledge.getDocumentCount(), knowledge.getStatus(),
                knowledge.getDescription(), knowledge.getSourceFilePath());
    }

    @Tool("新增一个知识库（注意：不支持通过AI上传文件，如需上传文件请使用Web界面）")
    public String createKnowledge(
            @P("知识库名称，必填") String name,
            @P("知识库类型，必填：document/qa/web") String type,
            @P("知识库描述，可选") String description) {
        KnowledgeVO result = knowledgeService.create(name, description, type, null);
        return "知识库创建成功！\n- ID：" + result.getId() + "\n- 名称：" + result.getName()
                + "\n- 类型：" + result.getType() + "\n- 文档数量：" + result.getDocumentCount();
    }

    @Tool("修改一个知识库")
    public String updateKnowledge(
            @P("知识库 ID，必填") String id,
            @P("知识库名称，必填") String name,
            @P("知识库类型，必填：document/qa/web") String type,
            @P("知识库描述，可选") String description) {
        KnowledgeDTO dto = new KnowledgeDTO();
        dto.setName(name);
        dto.setType(type);
        dto.setDescription(description);

        KnowledgeVO result = knowledgeService.update(Long.valueOf(id), dto);
        return "知识库修改成功！\n- ID：" + result.getId() + "\n- 名称：" + result.getName()
                + "\n- 类型：" + result.getType() + "\n- 文档数量：" + result.getDocumentCount();
    }

    @Tool("删除一个知识库（会同时删除知识库中的所有文件）")
    public String deleteKnowledge(@P("知识库 ID，必填") String id) {
        knowledgeService.delete(Long.valueOf(id));
        return "知识库（ID: " + id + "）及其所有文件已成功删除。";
    }

    @Tool("查询知识库中的文件列表")
    public String listKnowledgeFiles(@P("知识库 ID，必填") String knowledgeId) {
        List<KnowledgeFileVO> files = knowledgeService.listFiles(Long.valueOf(knowledgeId));
        if (files == null || files.isEmpty()) {
            return "该知识库中暂无文件。";
        }
        return files.stream()
                .map(f -> String.format("- 文件ID:%s | 原始文件名:%s | 大小:%d字节 | 类型:%s",
                        f.getId(), f.getOriginalName(), f.getSize(), f.getMimeType()))
                .collect(Collectors.joining("\n", "知识库文件列表（共" + files.size() + "个）：\n", ""));
    }

    @Tool("删除知识库中的指定文件")
    public String deleteKnowledgeFile(
            @P("知识库 ID，必填") String knowledgeId,
            @P("文件 ID，必填") String fileId) {
        knowledgeService.deleteFile(Long.valueOf(knowledgeId), fileId);
        return "知识库（ID: " + knowledgeId + "）中的文件（ID: " + fileId + "）已成功删除。";
    }

    // ==================== 配置相关（仅查询） ====================

    @Tool("查询系统中所有的配置列表，支持按关键词、状态筛选")
    public String querySetupList(
            @P("搜索关键词，可选") String keyword,
            @P("状态：active-启用，inactive-禁用，可选") String status) {
        PageVO<SetupVO> page = setupService.queryPage(1, 50, blankToNull(keyword), blankToNull(status));
        List<SetupVO> list = page.getList();
        if (list == null || list.isEmpty()) {
            return "系统中暂无配置。";
        }
        return list.stream()
                .map(s -> String.format("- ID:%s | 名称:%s | 状态:%s | 模型地址:%s",
                        s.getId(), s.getName(), s.getStatus(),
                        s.getBaseUrl() != null ? s.getBaseUrl() : "无"))
                .collect(Collectors.joining("\n", "系统配置列表（共" + list.size() + "个）：\n", ""));
    }

    // ==================== 用户管理相关 ====================

    @Tool("查询系统中所有的用户列表，支持按关键词、状态筛选")
    public String queryUserList(
            @P("搜索关键词（用户名），可选") String keyword,
            @P("状态：active-正常，inactive-禁用，可选") String status) {
        PageVO<UserVO> page = userService.queryPage(1, 50, blankToNull(keyword), blankToNull(status));
        List<UserVO> list = page.getList();
        if (list == null || list.isEmpty()) {
            return "系统中暂无用户。";
        }
        return list.stream()
                .map(u -> String.format("- ID:%d | 用户名:%s | 管理员等级:%s | 状态:%s",
                        u.getId(), u.getUsername(), formatAdminLevel(u.getAdmin()),
                        u.getStatus() == 1 ? "正常" : "禁用"))
                .collect(Collectors.joining("\n", "系统用户列表（共" + list.size() + "个）：\n", ""));
    }

    @Tool("根据 ID 查询用户的详细信息")
    public String queryUserById(@P("用户 ID") String id) {
        UserVO user = userService.queryById(Long.valueOf(id));
        if (user == null) {
            return "未找到 ID 为 " + id + " 的用户。";
        }
        return String.format(
                "用户详情：\n- ID：%d\n- 用户名：%s\n- 管理员等级：%s\n- 状态：%s\n- 头像：%s",
                user.getId(), user.getUsername(), formatAdminLevel(user.getAdmin()),
                user.getStatus() == 1 ? "正常" : "禁用",
                user.getAvatar() != null && !user.getAvatar().isEmpty() ? user.getAvatar() : "无");
    }

    @Tool("新增一个用户，默认密码为123456")
    public String createUser(
            @P("用户名，必填") String username,
            @P("管理员等级：2-超级管理员，1-管理员，0-普通用户，默认为0") String admin) {
        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setAdmin(parseInteger(admin) != null ? parseInteger(admin) : 0);

        UserVO result = userService.create(dto);
        return "用户创建成功！\n- ID：" + result.getId() + "\n- 用户名：" + result.getUsername()
                + "\n- 管理员等级：" + formatAdminLevel(result.getAdmin())
                + "\n- 默认密码：123456（请提醒用户尽快修改密码）";
    }

    @Tool("修改一个用户的信息")
    public String updateUser(
            @P("用户 ID，必填") String id,
            @P("用户名，必填") String username,
            @P("管理员等级：2-超级管理员，1-管理员，0-普通用户，可选") String admin,
            @P("新密码，可选，为空则不修改密码") String password) {
        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setAdmin(parseInteger(admin));
        dto.setPassword(blankToNull(password));

        UserVO result = userService.update(Long.valueOf(id), dto);
        String pwdMsg = (password != null && !password.isEmpty()) ? "，密码已更新" : "，密码未修改";
        return "用户修改成功！\n- ID：" + result.getId() + "\n- 用户名：" + result.getUsername()
                + "\n- 管理员等级：" + formatAdminLevel(result.getAdmin()) + pwdMsg;
    }

    @Tool("删除一个用户")
    public String deleteUser(@P("用户 ID，必填") String id) {
        userService.delete(Long.valueOf(id));
        return "用户（ID: " + id + "）已成功删除。";
    }

    @Tool("切换用户的启用/禁用状态")
    public String toggleUserStatus(
            @P("用户 ID，必填") String id,
            @P("目标状态：1-启用，0-禁用") String status) {
        UserStatusDTO dto = new UserStatusDTO();
        dto.setStatus(parseInteger(status));
        userService.toggleStatus(Long.valueOf(id), dto);
        String statusText = "1".equals(status) ? "启用" : "禁用";
        return "用户（ID: " + id + "）状态已切换为：" + statusText + "。";
    }

    // ==================== 数据看板相关 ====================

    @Tool("获取系统仪表盘概览数据，包含Token总量、Agent数量、知识库数量、工具数量等统计信息")
    public String getDataOverview() {
        DataOverviewVO overview = dataService.getOverview();
        StringBuilder sb = new StringBuilder();
        sb.append("=== 系统仪表盘概览 ===\n");
        sb.append("- Token 总用量：").append(overview.getTotalTokens()).append("\n");
        sb.append("- 已启用智能体数量：").append(overview.getActiveAgentCount()).append("\n");
        sb.append("- 智能体总数量：").append(overview.getTotalAgentCount()).append("\n");
        sb.append("- 知识库数量：").append(overview.getKnowledgeCount()).append("\n");
        sb.append("- 工具数量：").append(overview.getToolCount()).append("\n");

        if (overview.getAgentDistribution() != null && !overview.getAgentDistribution().isEmpty()) {
            sb.append("\n--- Agent 类型分布 ---\n");
            for (AgentDistributionVO dist : overview.getAgentDistribution()) {
                sb.append("- ").append(dist.getName()).append("：").append(dist.getValue()).append("个\n");
            }
        }

        if (overview.getAgentUsage() != null && !overview.getAgentUsage().isEmpty()) {
            sb.append("\n--- Agent Token 用量排行（Top 5）---\n");
            List<AgentUsageVO> top5 = overview.getAgentUsage().stream()
                    .limit(5).collect(Collectors.toList());
            int rank = 1;
            for (AgentUsageVO usage : top5) {
                sb.append(rank++).append(". ").append(usage.getName())
                        .append("：").append(usage.getTokens()).append(" tokens\n");
            }
        }
        return sb.toString();
    }

    @Tool("获取 Token 用量趋势数据")
    public String getTokenTrend() {
        List<TokenTrendVO> trend = dataService.getTokenTrend();
        if (trend == null || trend.isEmpty()) {
            return "暂无 Token 用量趋势数据。";
        }
        return trend.stream()
                .map(t -> "日期：" + t.getDate() + " | Token用量：" + t.getValue())
                .collect(Collectors.joining("\n", "Token 用量趋势：\n", ""));
    }

    @Tool("获取 Agent 用量排行")
    public String getAgentUsage() {
        List<AgentUsageVO> usage = dataService.getAgentUsage();
        if (usage == null || usage.isEmpty()) {
            return "暂无 Agent 用量数据。";
        }
        StringBuilder sb = new StringBuilder("Agent Token 用量排行：\n");
        int rank = 1;
        for (AgentUsageVO u : usage) {
            sb.append(rank++).append(". ").append(u.getName())
                    .append("：").append(u.getTokens()).append(" tokens\n");
        }
        return sb.toString();
    }

    @Tool("获取 Agent 类型分布统计")
    public String getAgentDistribution() {
        List<AgentDistributionVO> dist = dataService.getAgentDistribution();
        if (dist == null || dist.isEmpty()) {
            return "暂无 Agent 类型分布数据。";
        }
        return dist.stream()
                .map(d -> "类型：" + d.getName() + " | 数量：" + d.getValue() + "个")
                .collect(Collectors.joining("\n", "Agent 类型分布：\n", ""));
    }

    // ==================== 登录日志相关 ====================

    @Tool("查询登录日志列表，支持按用户名关键词、状态筛选")
    public String queryLoginLogList(
            @P("搜索关键词（用户名），可选") String keyword,
            @P("状态：success-成功，fail-失败，可选") String status) {
        PageVO<LoginLogVO> page = loginLogService.queryPage(1, 50, blankToNull(keyword), blankToNull(status));
        List<LoginLogVO> list = page.getList();
        if (list == null || list.isEmpty()) {
            return "暂无登录日志。";
        }
        return list.stream()
                .map(l -> String.format("- ID:%d | 用户:%s | IP:%s | 状态:%s | 时间:%s | 信息:%s",
                        l.getId(), l.getUsername(), l.getIp(), l.getStatus(),
                        l.getLoginTime(), l.getMessage()))
                .collect(Collectors.joining("\n", "登录日志列表（共" + list.size() + "条）：\n", ""));
    }

    @Tool("删除指定的登录日志，多个ID用逗号分隔，如 1,2,3")
    public String deleteLoginLogs(@P("日志ID列表，多个用逗号分隔，如 1,2,3") String ids) {
        List<Long> idList = parseLongList(ids);
        if (idList == null || idList.isEmpty()) {
            return "请提供要删除的日志 ID。";
        }
        loginLogService.deleteByIds(idList);
        return "已成功删除 " + idList.size() + " 条登录日志。";
    }

    @Tool("删除全部登录日志")
    public String deleteAllLoginLogs() {
        loginLogService.deleteAll();
        return "全部登录日志已成功删除。";
    }

    // ==================== 操作日志相关 ====================

    @Tool("查询操作日志列表，支持按用户名关键词、操作模块筛选")
    public String queryOperationLogList(
            @P("搜索关键词（用户名），可选") String keyword,
            @P("操作模块，可选，如：智能体管理/用户管理/知识库管理/工具管理/配置管理") String module) {
        PageVO<OperationLogVO> page = operationLogService.queryPage(1, 50, blankToNull(keyword), blankToNull(module));
        List<OperationLogVO> list = page.getList();
        if (list == null || list.isEmpty()) {
            return "暂无操作日志。";
        }
        return list.stream()
                .map(l -> String.format("- ID:%d | 用户:%s | 模块:%s | 操作:%s | 描述:%s | IP:%s | 时间:%s",
                        l.getId(), l.getUsername(), l.getModule(), l.getAction(),
                        l.getDescription(), l.getIp(), l.getCreateTime()))
                .collect(Collectors.joining("\n", "操作日志列表（共" + list.size() + "条）：\n", ""));
    }

    @Tool("删除指定的操作日志，多个ID用逗号分隔，如 1,2,3")
    public String deleteOperationLogs(@P("日志ID列表，多个用逗号分隔，如 1,2,3") String ids) {
        List<Long> idList = parseLongList(ids);
        if (idList == null || idList.isEmpty()) {
            return "请提供要删除的日志 ID。";
        }
        operationLogService.deleteByIds(idList);
        return "已成功删除 " + idList.size() + " 条操作日志。";
    }

    @Tool("删除全部操作日志")
    public String deleteAllOperationLogs() {
        operationLogService.deleteAll();
        return "全部操作日志已成功删除。";
    }

    // ==================== 辅助方法 ====================

    private String blankToNull(String str) {
        return (str == null || str.isBlank()) ? null : str;
    }

    private Long parseLong(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<Long> parseLongList(String str) {
        if (str == null || str.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return Arrays.stream(str.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
    }

    private String formatAdminLevel(Integer admin) {
        if (admin == null) {
            return "未知";
        }
        switch (admin) {
            case 2: return "超级管理员";
            case 1: return "管理员";
            case 0: return "普通用户";
            default: return "未知";
        }
    }

    private String formatAgentDetail(AgentVO agent) {
        return String.format(
                "- ID：%s\n- 名称：%s\n- 类型：%s\n- 状态：%s\n- 描述：%s\n- Token用量：%d\n- 关联配置ID：%s\n- 关联知识库ID：%s",
                agent.getId(), agent.getName(), agent.getType(), agent.getStatus(),
                agent.getDescription(), agent.getTokens() != null ? agent.getTokens() : 0,
                agent.getSetupId(), agent.getKnowledgeIds());
    }
}