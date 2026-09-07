package com.tianji.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.agent.config.crypto.CryptoService;
import com.tianji.agent.config.common.OperationLogUtil;
import com.tianji.agent.mapper.SetupMapper;
import com.tianji.agent.model.dto.SetupDTO;
import com.tianji.agent.model.dto.StatusDTO;
import com.tianji.agent.model.entity.Setup;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.model.vo.SetupOptionVO;
import com.tianji.agent.model.vo.SetupVO;
import com.tianji.agent.service.SetupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配置管理服务实现
 * <p>
 * 实现 {@link SetupService} 接口，完成配置管理的完整业务流程：
 * 分页查询、模糊搜索、按 ID 查询、新增、修改、删除、状态切换和全部查询。
 * </p>
 *
 * <p><b>安全说明：</b></p>
 * <ul>
 *   <li>新增/修改时：apiKey 明文 → AES 加密 → 存入数据库</li>
 *   <li>查询时：数据库密文 → AES 解密 → 返回前端明文</li>
 *   <li>数据库仅存储密文，即使数据库泄露也无法直接获取 apiKey</li>
 * </ul>
 */
@Service
@Slf4j
@AllArgsConstructor
public class SetupServiceImpl implements SetupService {

    /** 配置管理持久层 Mapper */
    private final SetupMapper setupMapper;

    /** 加密解密服务组件 */
    private final CryptoService cryptoService;

    /**
     * 将前端状态字符串转换为数据库存储的整数值
     *
     * @param status 前端状态字符串：active / inactive
     * @return 数据库状态值：1 / 0，输入为 null 时返回 null
     */
    private static Integer statusToInt(String status) {
        if (status == null) {
            return null;
        }
        return "active".equals(status) ? 1 : 0;
    }

    /**
     * 对实体中的 apiKey 进行解密，用于返回给前端
     *
     * @param entity 数据库实体（apiKey 为密文）
     */
    private void decryptApiKey(Setup entity) {
        if (entity != null && entity.getApiKey() != null) {
            entity.setApiKey(cryptoService.decrypt(entity.getApiKey()));
        }
    }

    /**
     * 对实体中的 apiKey 进行加密，用于存入数据库
     *
     * @param entity 待保存的实体（apiKey 为明文）
     */
    private void encryptApiKey(Setup entity) {
        if (entity != null && entity.getApiKey() != null) {
            entity.setApiKey(cryptoService.encrypt(entity.getApiKey()));
        }
    }

    @Override
    public PageVO<SetupVO> queryPage(Integer page, Integer pageSize, String keyword, String status) {
        log.info("分页查询配置: page={}, pageSize={}, keyword={}, status={}", page, pageSize, keyword, status);

        Page<Setup> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setup> queryWrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Setup::getName, keyword);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Setup::getStatus, statusToInt(status));
        }
        queryWrapper.orderByDesc(Setup::getCreateTime);

        Page<Setup> entityPage = setupMapper.selectPage(pageParam, queryWrapper);
        List<SetupVO> voList = entityPage.getRecords().stream()
                .peek(this::decryptApiKey)
                .map(SetupVO::fromEntity)
                .collect(Collectors.toList());

        Page<SetupVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return PageVO.fromPage(voPage);
    }

    @Override
    public List<SetupVO> queryByName(String name) {
        log.info("按名称模糊查询配置: {}", name);

        LambdaQueryWrapper<Setup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Setup::getName, name);
        queryWrapper.orderByDesc(Setup::getCreateTime);

        List<Setup> entities = setupMapper.selectList(queryWrapper);
        return entities.stream()
                .peek(this::decryptApiKey)
                .map(SetupVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public SetupVO queryById(Long id) {
        log.info("按ID查询配置: {}", id);

        Setup entity = setupMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("配置不存在");
        }
        decryptApiKey(entity);
        return SetupVO.fromEntity(entity);
    }

    @Override
    public SetupVO create(SetupDTO dto) {
        log.info("新增配置: {}", dto.getName());

        Setup entity = new Setup();
        entity.setName(dto.getName());
        entity.setApiKey(dto.getApiKey());
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setStatus(1);

        encryptApiKey(entity);
        setupMapper.insert(entity);
        log.info("配置创建成功, id: {}", entity.getId());

        // 记录详细的操作日志
        String maskedApiKey = maskApiKey(dto.getApiKey());
        OperationLogUtil.log("系统设置", "新增", 
            String.format("新增配置【%s】，API密钥：%s，模型地址：%s", dto.getName(), maskedApiKey, dto.getBaseUrl()));

        decryptApiKey(entity);
        return SetupVO.fromEntity(entity);
    }

    @Override
    public SetupVO update(Long id, SetupDTO dto) {
        log.info("修改配置: id={}, name={}", id, dto.getName());

        Setup oldEntity = setupMapper.selectById(id);
        if (oldEntity == null) {
            throw new RuntimeException("配置不存在");
        }

        // 记录修改前的数据用于日志对比
        String oldName = oldEntity.getName();
        String oldBaseUrl = oldEntity.getBaseUrl();

        Setup entity = new Setup();
        entity.setId(oldEntity.getId());
        entity.setName(dto.getName());
        entity.setApiKey(dto.getApiKey());
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setStatus(oldEntity.getStatus());

        encryptApiKey(entity);
        setupMapper.updateById(entity);
        log.info("配置修改成功, id: {}", id);

        // 记录详细的操作日志（对比修改前后）
        List<String> changes = new ArrayList<>();
        if (!oldName.equals(dto.getName())) {
            changes.add(String.format("名称: %s -> %s", oldName, dto.getName()));
        }
        if (dto.getBaseUrl() != null && !oldBaseUrl.equals(dto.getBaseUrl())) {
            changes.add(String.format("模型地址: %s -> %s", oldBaseUrl, dto.getBaseUrl()));
        }
        if (dto.getApiKey() != null && !dto.getApiKey().isEmpty()) {
            changes.add("API密钥已修改");
        }
        
        if (!changes.isEmpty()) {
            OperationLogUtil.log("系统设置", "修改", 
                String.format("修改配置【%s】，ID：%d | %s", dto.getName(), id, String.join("；", changes)));
        } else {
            OperationLogUtil.log("系统设置", "修改", 
                String.format("修改配置【%s】，ID：%d（无实质变更）", dto.getName(), id));
        }

        decryptApiKey(entity);
        return SetupVO.fromEntity(entity);
    }

    @Override
    public void delete(Long id) {
        log.info("删除配置: {}", id);

        Setup entity = setupMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("配置不存在");
        }

        // 记录删除的详细信息
        String maskedApiKey = maskApiKey(entity.getApiKey());
        OperationLogUtil.log("系统设置", "删除", 
            String.format("删除配置【%s】，API密钥：%s，配置ID：%d", entity.getName(), maskedApiKey, id));

        setupMapper.deleteById(id);
        log.info("配置删除成功, id: {}", id);
    }

    @Override
    public String toggleStatus(Long id, StatusDTO dto) {
        log.info("切换配置状态: id={}, targetStatus={}", id, dto.getStatus());

        Setup entity = setupMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("配置不存在");
        }

        String oldStatus = entity.getStatus() == 1 ? "启用" : "禁用";
        String newStatus = "active".equals(dto.getStatus()) ? "启用" : "禁用";

        Integer newStatusInt = statusToInt(dto.getStatus());
        entity.setStatus(newStatusInt);
        setupMapper.updateById(entity);

        String resultStatus = newStatusInt == 1 ? "active" : "inactive";
        log.info("配置状态切换成功: id={}, status={}", id, resultStatus);
        
        // 记录状态切换的详细日志
        OperationLogUtil.log("系统设置", "切换状态", 
            String.format("切换配置【%s】状态：%s -> %s，配置ID：%d", entity.getName(), oldStatus, newStatus, id));
        
        return resultStatus;
    }

    /**
     * 脱敏显示API密钥
     *
     * @param apiKey API密钥
     * @return 脱敏后的密钥
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "未设置";
        }
        if (apiKey.length() <= 8) {
            return "***" + apiKey.substring(apiKey.length() - 2);
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    @Override
    public List<SetupVO> queryAll() {
        log.info("查询全部配置");

        LambdaQueryWrapper<Setup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setup::getStatus, 1);
        queryWrapper.orderByDesc(Setup::getCreateTime);

        List<Setup> entities = setupMapper.selectList(queryWrapper);
        return entities.stream()
                .peek(this::decryptApiKey)
                .map(SetupVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SetupOptionVO> queryOptions() {
        log.info("查询配置选项列表（下拉选择专用）");

        LambdaQueryWrapper<Setup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setup::getStatus, 1);
        queryWrapper.orderByDesc(Setup::getCreateTime);

        List<Setup> entities = setupMapper.selectList(queryWrapper);
        return entities.stream()
                .map(SetupOptionVO::fromEntity)
                .collect(Collectors.toList());
    }
}