package com.tianji.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.agent.config.common.OperationLogUtil;
import com.tianji.agent.mapper.AgentKnowledgeMapper;
import com.tianji.agent.mapper.AgentMapper;
import com.tianji.agent.model.dto.AgentDTO;
import com.tianji.agent.model.dto.StatusDTO;
import com.tianji.agent.model.entity.Agent;
import com.tianji.agent.model.entity.AgentKnowledge;
import com.tianji.agent.model.vo.AgentVO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.service.AgentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能体管理服务实现
 * <p>
 * 实现 {@link AgentService} 接口，完成智能体管理的完整业务流程：
 * 分页查询、按 ID 查询、新增、修改、删除、状态切换和全部查询。
 * </p>
 *
 * <p><b>数据说明：</b></p>
 * <ul>
 *   <li>智能体名称（name）用于唯一标识一个 AI Agent 实例</li>
 *   <li>关联配置（setupId）指向 setup 表，一对一关系</li>
 *   <li>关联知识库（knowledgeIds）通过 agent_knowledge 中间表存储，多对多关系</li>
 *   <li>Token 用量（tokens）用于追踪智能体的 Token 消耗</li>
 *   <li>状态（status）：1-启用（active），0-禁用（inactive）</li>
 * </ul>
 */
@Service
@Slf4j
public class AgentServiceImpl implements AgentService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 智能体管理持久层 Mapper */
    private final AgentMapper agentMapper;

    /** 智能体知识库关联持久层 Mapper */
    private final AgentKnowledgeMapper agentKnowledgeMapper;

    public AgentServiceImpl(AgentMapper agentMapper,
                            AgentKnowledgeMapper agentKnowledgeMapper) {
        this.agentMapper = agentMapper;
        this.agentKnowledgeMapper = agentKnowledgeMapper;
    }

    /**
     * 将前端状态字符串转换为数据库存储的整数值
     *
     * @param status 前端状态字符串（active/inactive）
     * @return 数据库状态值（1/0），null 返回 null
     */
    private static Integer statusToInt(String status) {
        if (status == null) {
            return null;
        }
        return "active".equals(status) ? 1 : 0;
    }

    // ==================== JSON 序列化 ====================

    /**
     * 将 Long 列表转换为 JSON 数组字符串
     *
     * @param idList Long 列表
     * @return JSON 数组字符串，如 "[1, 2, 3]"
     */
    private String toJsonArray(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return "[]";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(idList);
        } catch (Exception e) {
            log.warn("序列化JSON数组失败", e);
            return "[]";
        }
    }

    // ==================== 中间表操作 ====================

    /**
     * 查询智能体关联的知识库ID列表
     *
     * @param agentId 智能体ID
     * @return 知识库ID列表
     */
    private List<Long> queryKnowledgeIds(Long agentId) {
        LambdaQueryWrapper<AgentKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentKnowledge::getAgentId, agentId);
        List<AgentKnowledge> relations = agentKnowledgeMapper.selectList(wrapper);
        return relations.stream()
                .map(AgentKnowledge::getKnowledgeId)
                .collect(Collectors.toList());
    }

    /**
     * 保存智能体与知识库的关联关系
     * <p>
     * 先删除旧关联，再批量插入新关联。
     * </p>
     *
     * @param agentId      智能体ID
     * @param knowledgeIds 知识库ID列表
     */
    private void saveKnowledgeRelations(Long agentId, List<Long> knowledgeIds) {
        LambdaQueryWrapper<AgentKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentKnowledge::getAgentId, agentId);
        agentKnowledgeMapper.delete(wrapper);

        if (knowledgeIds != null) {
            for (Long knowledgeId : knowledgeIds) {
                AgentKnowledge relation = new AgentKnowledge();
                relation.setAgentId(agentId);
                relation.setKnowledgeId(knowledgeId);
                agentKnowledgeMapper.insert(relation);
            }
        }
        log.info("智能体知识库关联更新: agentId={}, count={}", agentId,
                knowledgeIds != null ? knowledgeIds.size() : 0);
    }

    /**
     * 删除智能体与知识库的全部关联
     *
     * @param agentId 智能体ID
     */
    private void deleteKnowledgeRelations(Long agentId) {
        LambdaQueryWrapper<AgentKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentKnowledge::getAgentId, agentId);
        agentKnowledgeMapper.delete(wrapper);
        log.info("删除智能体知识库关联: agentId={}", agentId);
    }

    // ==================== 业务方法 ====================

    @Override
    public PageVO<AgentVO> queryPage(Integer page, Integer pageSize, String keyword, String type, String status) {
        log.info("分页查询智能体: page={}, pageSize={}, keyword={}, type={}, status={}", page, pageSize, keyword, type, status);

        Page<Agent> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Agent::getName, keyword);
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(Agent::getType, type);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Agent::getStatus, statusToInt(status));
        }
        queryWrapper.orderByDesc(Agent::getCreateTime);

        Page<Agent> entityPage = agentMapper.selectPage(pageParam, queryWrapper);
        List<AgentVO> voList = entityPage.getRecords().stream()
                .map(entity -> AgentVO.fromEntity(entity,
                        queryKnowledgeIds(entity.getId())))
                .collect(Collectors.toList());

        Page<AgentVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return PageVO.fromPage(voPage);
    }

    @Override
    public AgentVO queryById(Long id) {
        log.info("按ID查询智能体: {}", id);

        Agent entity = agentMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("智能体不存在");
        }
        return AgentVO.fromEntity(entity,
                queryKnowledgeIds(entity.getId()));
    }

    @Override
    @Transactional
    public AgentVO create(AgentDTO dto) {
        log.info("新增智能体: {}", dto.getName());

        Agent entity = new Agent();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setSetupId(dto.getSetupId());
        entity.setTokens(dto.getTokens());
        entity.setStatus(1);

        agentMapper.insert(entity);
        log.info("智能体记录创建成功, id: {}", entity.getId());

        saveKnowledgeRelations(entity.getId(), dto.getKnowledgeIds());

        // 记录详细的操作日志
        OperationLogUtil.log("智能体管理", "新增", 
            String.format("新增智能体【%s】，类型：%s", dto.getName(), dto.getType()));

        return AgentVO.fromEntity(entity,
                dto.getKnowledgeIds());
    }

    @Override
    @Transactional
    public AgentVO update(Long id, AgentDTO dto) {
        log.info("修改智能体: id={}, name={}", id, dto.getName());

        Agent oldEntity = agentMapper.selectById(id);
        if (oldEntity == null) {
            throw new RuntimeException("智能体不存在");
        }

        // 记录修改前的数据用于日志对比
        String oldName = oldEntity.getName();
        String oldType = oldEntity.getType();
        Long oldSetupId = oldEntity.getSetupId();

        Agent entity = new Agent();
        entity.setId(oldEntity.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setSetupId(dto.getSetupId());
        entity.setTokens(dto.getTokens());
        entity.setStatus(oldEntity.getStatus());

        agentMapper.updateById(entity);
        log.info("智能体修改成功, id: {}", id);

        saveKnowledgeRelations(id, dto.getKnowledgeIds());

        // 记录详细的操作日志（对比修改前后）
        List<String> changes = new ArrayList<>();
        if (!oldName.equals(dto.getName())) {
            changes.add(String.format("名称: %s -> %s", oldName, dto.getName()));
        }
        if (dto.getType() != null && !oldType.equals(dto.getType())) {
            changes.add(String.format("类型: %s -> %s", oldType, dto.getType()));
        }
        if (dto.getSetupId() != null && !oldSetupId.equals(dto.getSetupId())) {
            changes.add(String.format("配置ID: %d -> %d", oldSetupId, dto.getSetupId()));
        }
        
        if (!changes.isEmpty()) {
            OperationLogUtil.log("智能体管理", "修改", 
                String.format("修改智能体【%s】，ID：%d | %s", dto.getName(), id, String.join("；", changes)));
        } else {
            OperationLogUtil.log("智能体管理", "修改", 
                String.format("修改智能体【%s】，ID：%d（无实质变更）", dto.getName(), id));
        }

        return AgentVO.fromEntity(entity,
                dto.getKnowledgeIds());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("删除智能体: {}", id);

        Agent entity = agentMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("智能体不存在");
        }

        // 记录删除的详细信息
        OperationLogUtil.log("智能体管理", "删除", 
            String.format("删除智能体【%s】，类型：%s，智能体ID：%d", entity.getName(), entity.getType(), id));

        deleteKnowledgeRelations(id);
        agentMapper.deleteById(id);
        log.info("智能体删除成功, id: {}", id);
    }

    @Override
    public String toggleStatus(Long id, StatusDTO dto) {
        log.info("切换智能体状态: id={}, targetStatus={}", id, dto.getStatus());

        Agent entity = agentMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("智能体不存在");
        }

        String oldStatus = entity.getStatus() == 1 ? "启用" : "禁用";
        String newStatus = "active".equals(dto.getStatus()) ? "启用" : "禁用";

        Integer newStatusInt = statusToInt(dto.getStatus());
        entity.setStatus(newStatusInt);
        agentMapper.updateById(entity);
        log.info("智能体状态切换成功, id: {}, status: {}", id, dto.getStatus());

        // 记录状态切换的详细日志
        OperationLogUtil.log("智能体管理", "切换状态", 
            String.format("切换智能体【%s】状态：%s -> %s，智能体ID：%d", entity.getName(), oldStatus, newStatus, id));

        return dto.getStatus();
    }

    @Override
    public List<AgentVO> queryAll() {
        log.info("查询全部智能体");

        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Agent::getStatus, 1);
        queryWrapper.orderByDesc(Agent::getCreateTime);

        List<Agent> entities = agentMapper.selectList(queryWrapper);
        return entities.stream()
                .map(entity -> AgentVO.fromEntity(entity,
                        queryKnowledgeIds(entity.getId())))
                .collect(Collectors.toList());
    }
}