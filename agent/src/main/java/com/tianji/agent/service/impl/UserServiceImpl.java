package com.tianji.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.agent.config.crypto.CryptoService;
import com.tianji.agent.config.jwt.BaseContext;
import com.tianji.agent.config.common.OperationLogUtil;
import com.tianji.agent.mapper.UserMapper;
import com.tianji.agent.model.dto.ChangePasswordDTO;
import com.tianji.agent.model.dto.ProfileUpdateDTO;
import com.tianji.agent.model.dto.UserDTO;
import com.tianji.agent.model.dto.UserStatusDTO;
import com.tianji.agent.model.entity.User;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.model.vo.UserVO;
import com.tianji.agent.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现
 * <p>
 * 实现 {@link UserService} 接口，完成用户管理的完整业务流程：
 * 分页查询、按ID查询、新增、修改、删除和状态切换。
 * </p>
 *
 * <p><b>数据说明：</b></p>
 * <ul>
 *   <li>用户名（username）用于登录账号，全局唯一</li>
 *   <li>密码（password）在新增时默认值为 "123456"</li>
 *   <li>管理员等级（admin）：2-超级管理员，1-管理员，0-普通用户</li>
 *   <li>状态（status）：1-正常，0-禁用</li>
 *   <li>删除操作为逻辑删除，而非物理删除</li>
 * </ul>
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    /** 用户管理持久层 Mapper */
    private final UserMapper userMapper;

    /** 加密解密服务组件 */
    private final CryptoService cryptoService;

    @Override
    public PageVO<UserVO> queryPage(Integer page, Integer pageSize, String keyword, String status) {
        log.info("分页查询用户: page={}, pageSize={}, keyword={}, status={}", page, pageSize, keyword, status);

        Page<User> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(User::getUsername, keyword);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(User::getStatus, statusToInt(status));
        }
        queryWrapper.orderByAsc(User::getId).orderByDesc(User::getAdmin);

        Page<User> entityPage = userMapper.selectPage(pageParam, queryWrapper);
        List<UserVO> voList = entityPage.getRecords().stream()
                .peek(this::decryptPassword)
                .map(UserVO::fromEntity)
                .collect(Collectors.toList());

        Page<UserVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return PageVO.fromPage(voPage);
    }

    @Override
    public UserVO queryById(Long id) {
        log.info("按ID查询用户: {}", id);

        User entity = userMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("用户不存在");
        }
        decryptPassword(entity);
        return UserVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public UserVO create(UserDTO dto) {
        log.info("新增用户: {}", dto.getUsername());

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User entity = new User();
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword() != null ? dto.getPassword() : "123456");
        entity.setAvatar(dto.getAvatar() != null ? dto.getAvatar() : "");
        entity.setAdmin(dto.getAdmin() != null ? dto.getAdmin() : 0);
        entity.setStatus(1);

        encryptPassword(entity);
        userMapper.insert(entity);
        log.info("用户创建成功, id: {}", entity.getId());

        // 记录详细的操作日志
        Integer adminLevel = dto.getAdmin() != null ? dto.getAdmin() : 0;
        String adminLevelText = getAdminLevelText(adminLevel);
        OperationLogUtil.log("用户管理", "新增", 
            String.format("新增用户【%s】，管理员等级：%s", dto.getUsername(), adminLevelText));

        decryptPassword(entity);
        return UserVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public UserVO update(Long id, UserDTO dto) {
        log.info("修改用户: id={}, username={}", id, dto.getUsername());

        User oldEntity = userMapper.selectById(id);
        if (oldEntity == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!oldEntity.getUsername().equals(dto.getUsername())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, dto.getUsername());
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new RuntimeException("用户名已存在");
            }
        }

        // 记录修改前的数据用于日志对比
        String oldUsername = oldEntity.getUsername();
        Integer oldAdmin = oldEntity.getAdmin();
        String oldPassword = oldEntity.getPassword();

        User entity = new User();
        entity.setId(oldEntity.getId());
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword() != null && !dto.getPassword().isEmpty() ? dto.getPassword() : oldPassword);
        entity.setAvatar(dto.getAvatar() != null ? dto.getAvatar() : oldEntity.getAvatar());
        entity.setAdmin(dto.getAdmin() != null ? dto.getAdmin() : oldAdmin);
        entity.setStatus(oldEntity.getStatus());

        encryptPassword(entity);
        userMapper.updateById(entity);
        log.info("用户修改成功, id: {}", id);

        // 记录详细的操作日志（对比修改前后）
        List<String> changes = new ArrayList<>();
        if (!oldUsername.equals(dto.getUsername())) {
            changes.add(String.format("用户名: %s -> %s", oldUsername, dto.getUsername()));
        }
        if (dto.getAdmin() != null && !oldAdmin.equals(dto.getAdmin())) {
            changes.add(String.format("管理员等级: %s -> %s", getAdminLevelText(oldAdmin), getAdminLevelText(dto.getAdmin())));
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            changes.add("密码已修改");
        }
        
        if (!changes.isEmpty()) {
            OperationLogUtil.log("用户管理", "修改", 
                String.format("修改用户【%s】，ID：%d | %s", dto.getUsername(), id, String.join("；", changes)));
        } else {
            OperationLogUtil.log("用户管理", "修改", 
                String.format("修改用户【%s】，ID：%d（无实质变更）", dto.getUsername(), id));
        }

        decryptPassword(entity);
        return UserVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("删除用户: {}", id);

        User entity = userMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("用户不存在");
        }

        // 记录删除的详细信息
        String adminLevelText = getAdminLevelText(entity.getAdmin());
        OperationLogUtil.log("用户管理", "删除", 
            String.format("删除用户【%s】，管理员等级：%s，用户ID：%d", entity.getUsername(), adminLevelText, id));

        userMapper.deleteById(id);
        log.info("用户删除成功, id: {}", id);
    }

    @Override
    public void toggleStatus(Long id, UserStatusDTO dto) {
        log.info("切换用户状态: id={}, targetStatus={}", id, dto.getStatus());

        User entity = userMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("用户不存在");
        }

        String oldStatus = entity.getStatus() == 1 ? "启用" : "禁用";
        String newStatus = dto.getStatus() == 1 ? "启用" : "禁用";

        entity.setStatus(dto.getStatus());
        userMapper.updateById(entity);
        log.info("用户状态切换成功: id={}, status={}", id, dto.getStatus());

        // 记录状态切换的详细日志
        OperationLogUtil.log("用户管理", "切换状态", 
            String.format("切换用户【%s】状态：%s -> %s，用户ID：%d", entity.getUsername(), oldStatus, newStatus, id));
    }

    /**
     * 获取管理员等级文本
     *
     * @param admin 管理员等级
     * @return 等级文本
     */
    private String getAdminLevelText(Integer admin) {
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

    /**
     * 将状态字符串转换为整数值
     *
     * @param status 状态字符串（active-正常，inactive-禁用）
     * @return 状态整数值（1-正常，0-禁用）
     */
    private static Integer statusToInt(String status) {
        if (status == null) {
            return null;
        }
        return "active".equals(status) ? 1 : 0;
    }

    @Override
    public UserVO getProfile() {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("获取当前用户信息: userId={}", currentUserId);

        User entity = userMapper.selectById(currentUserId);
        if (entity == null) {
            throw new RuntimeException("用户不存在");
        }

        decryptPassword(entity);
        return UserVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public UserVO updateProfile(ProfileUpdateDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("当前用户修改个人信息: userId={}, username={}", currentUserId, dto.getUsername());

        User entity = userMapper.selectById(currentUserId);
        if (entity == null) {
            throw new RuntimeException("用户不存在");
        }

        String oldUsername = entity.getUsername();
        String oldAvatar = entity.getAvatar();

        if (!entity.getUsername().equals(dto.getUsername())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, dto.getUsername());
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new RuntimeException("用户名已存在");
            }
        }

        entity.setUsername(dto.getUsername());
        if (dto.getAvatar() != null) {
            entity.setAvatar(dto.getAvatar());
        }
        userMapper.updateById(entity);
        log.info("个人信息修改成功: userId={}, newUsername={}", currentUserId, dto.getUsername());

        List<String> changes = new ArrayList<>();
        if (!oldUsername.equals(dto.getUsername())) {
            changes.add(String.format("用户名: %s -> %s", oldUsername, dto.getUsername()));
        }
        if (dto.getAvatar() != null && !dto.getAvatar().equals(oldAvatar)) {
            changes.add("头像已更新");
        }

        if (!changes.isEmpty()) {
            OperationLogUtil.log("用户管理", "修改个人信息",
                    String.format("用户【%s】修改个人信息 | %s", oldUsername, String.join("；", changes)));
        }

        UserVO vo = UserVO.fromEntity(entity);
        vo.setPassword(null);
        return vo;
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("当前用户修改密码: userId={}", currentUserId);

        User entity = userMapper.selectById(currentUserId);
        if (entity == null) {
            throw new RuntimeException("用户不存在");
        }

        String decryptedPassword = cryptoService.decrypt(entity.getPassword());
        if (!decryptedPassword.equals(dto.getOldPassword())) {
            throw new RuntimeException("原密码错误");
        }

        entity.setPassword(dto.getNewPassword());
        encryptPassword(entity);
        userMapper.updateById(entity);
        log.info("密码修改成功: userId={}", currentUserId);

        OperationLogUtil.log("用户管理", "修改密码",
                String.format("用户【%s】修改了登录密码", entity.getUsername()));
    }

    /**
     * 对实体中的 password 进行解密，用于返回给前端
     *
     * @param entity 数据库实体（password 为密文）
     */
    private void decryptPassword(User entity) {
        if (entity != null && entity.getPassword() != null) {
            entity.setPassword(cryptoService.decrypt(entity.getPassword()));
        }
    }

    /**
     * 对实体中的 password 进行加密，用于存入数据库
     *
     * @param entity 待保存的实体（password 为明文）
     */
    private void encryptPassword(User entity) {
        if (entity != null && entity.getPassword() != null) {
            entity.setPassword(cryptoService.encrypt(entity.getPassword()));
        }
    }
}