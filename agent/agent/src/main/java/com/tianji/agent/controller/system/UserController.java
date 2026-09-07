package com.tianji.agent.controller.system;

import com.tianji.agent.config.common.Result;
import com.tianji.agent.config.upload.FileUploadUtil;
import com.tianji.agent.model.dto.ChangePasswordDTO;
import com.tianji.agent.model.dto.ProfileUpdateDTO;
import com.tianji.agent.model.dto.UserDTO;
import com.tianji.agent.model.dto.UserStatusDTO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.model.vo.UserVO;
import com.tianji.agent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 用户管理控制器
 * <p>
 * 提供用户管理的 RESTful API 接口，包括分页查询、按ID查询、新增、修改、删除和状态切换。
 * </p>
 *
 * <p><b>接口路径：</b>{@code /api/system/user}</p>
 * <p><b>请求方式：</b>GET/POST/PUT/DELETE/PATCH</p>
 *
 * <p><b>接口列表：</b></p>
 * <ul>
 *   <li>GET /api/system/user/list - 分页查询用户列表</li>
 *   <li>GET /api/system/user/profile - 获取当前用户信息（含密码明文）</li>
 *   <li>PUT /api/system/user/profile - 修改当前用户信息（仅用户名）</li>
 *   <li>POST /api/system/user/change-pwd - 修改当前用户密码</li>
 *   <li>GET /api/system/user/{id} - 根据ID查询用户</li>
 *   <li>POST /api/system/user/create - 新增用户</li>
 *   <li>PUT /api/system/user/{id} - 修改用户（管理员）</li>
 *   <li>DELETE /api/system/user/delete/{id} - 删除用户</li>
 *   <li>PATCH /api/system/user/{id}/status - 切换用户状态</li>
 *   <li>POST /api/upload/avatar - 上传头像</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/system/user")
@Tag(name = "用户管理", description = "用户管理相关接口")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    /** 用户管理服务 */
    private final UserService userService;

    /** 头像文件存储目录 */
    @Value("${agent.upload.avatar-dir:static/uploads/avatars}")
    private String avatarDir;

    /** 服务器基础 URL */
    @Value("${server.base-url:http://localhost:8081}")
    private String baseUrl;

    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表", description = "支持按用户名模糊搜索和状态筛选")
    public Result<PageVO<UserVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        PageVO<UserVO> result = userService.queryPage(page, pageSize, keyword, status);
        return Result.success(result);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息，包含密码明文，用于前端个人信息页面展示")
    public Result<UserVO> profile() {
        UserVO result = userService.getProfile();
        return Result.success(result);
    }

    @PutMapping("/profile")
    @Operation(summary = "修改当前用户信息", description = "当前登录用户修改自己的用户名，2~20字符，返回不含密码的用户信息")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        UserVO result = userService.updateProfile(dto);
        return Result.success(result);
    }

    @PostMapping("/change-pwd")
    @Operation(summary = "修改当前用户密码", description = "当前登录用户修改自己的密码，需验证原密码正确后才能更新")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(dto);
        return Result.success("密码修改成功", null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户", description = "返回指定ID的用户详情")
    public Result<UserVO> getById(@PathVariable Long id) {
        UserVO result = userService.queryById(id);
        return Result.success(result);
    }

    @PostMapping("/create")
    @Operation(summary = "新增用户", description = "创建新的用户记录")
    public Result<UserVO> create(@Valid @RequestBody UserDTO dto) {
        UserVO result = userService.create(dto);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改用户", description = "根据ID更新用户信息，密码为可选字段")
    public Result<UserVO> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        UserVO result = userService.update(id, dto);
        return Result.success(result);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除用户", description = "逻辑删除指定用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除成功", null);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "切换用户状态", description = "切换用户的启用/禁用状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @Valid @RequestBody UserStatusDTO dto) {
        userService.toggleStatus(id, dto);
        return Result.success();
    }

    /**
     * 上传头像
     * <p>
     * 接收用户上传的头像图片文件，校验文件类型和大小后保存到服务器，
     * 返回头像的访问 URL。前端拿到 URL 后，再通过 PUT /api/system/user/profile 更新到数据库。
     * </p>
     *
     * @param file 要上传的头像图片文件
     * @return 统一响应结果，data 包含头像访问 URL
     */
    @PostMapping("/upload/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像图片文件，返回头像访问URL")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("用户上传头像: fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        // 校验文件
        FileUploadUtil.validateImage(file);

        // 确保目录存在
        Path avatarPath = Paths.get(avatarDir).toAbsolutePath().normalize();
        FileUploadUtil.ensureDirectoryExists(avatarPath);

        // 生成唯一文件名
        String storedFilename = FileUploadUtil.generateAvatarFilename(file.getOriginalFilename());
        Path targetPath = avatarPath.resolve(storedFilename);

        // 保存文件
        FileUploadUtil.saveFile(file, targetPath);

        // 生成访问 URL
        String avatarUrl = baseUrl + "/static/uploads/avatars/" + storedFilename;
        log.info("头像上传成功，访问URL: {}", avatarUrl);

        return Result.success("上传成功", avatarUrl);
    }
}