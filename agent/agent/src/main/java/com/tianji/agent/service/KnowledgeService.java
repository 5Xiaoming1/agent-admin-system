package com.tianji.agent.service;

import com.tianji.agent.model.dto.KnowledgeDTO;
import com.tianji.agent.model.vo.KnowledgeFileVO;
import com.tianji.agent.model.vo.KnowledgeVO;
import com.tianji.agent.model.vo.PageVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库管理服务接口
 * <p>
 * 定义知识库管理的核心业务逻辑，包括知识库的增删改查。
 * 知识库的实际文档内容存储在文件系统中，按类型分目录组织。
 * 由 {@link com.tianji.agent.service.impl.KnowledgeServiceImpl} 提供具体实现。
 * </p>
 */
public interface KnowledgeService {

    /**
     * 分页查询知识库列表
     * <p>
     * 支持按知识库名称关键词模糊搜索、按类型筛选。
     * </p>
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  知识库名称关键词（可选），为空则查询全部
     * @param type     知识库类型筛选（可选），document/qa/web，为空则查询全部
     * @return 分页结果，包含知识库列表和分页信息
     */
    PageVO<KnowledgeVO> queryPage(Integer page, Integer pageSize, String keyword, String type);

    /**
     * 根据 ID 查询知识库
     *
     * @param id 知识库 ID
     * @return 知识库详情，不存在时抛出异常
     */
    KnowledgeVO queryById(Long id);

    /**
     * 新增知识库
     * <p>
     * 创建新的知识库记录，默认状态为启用，同时创建对应的文件存储目录。
     * 如果提供了文件，则将文件保存到知识库目录中。
     * </p>
     *
     * @param name        知识库名称
     * @param description 知识库描述
     * @param type        知识库类型：document/qa/web
     * @param file        初始文档文件（可选）
     * @return 创建后的知识库信息
     */
    KnowledgeVO create(String name, String description, String type, MultipartFile file);

    /**
     * 修改知识库
     * <p>
     * 更新知识库的基本信息，如果类型变更则迁移文件存储目录。
     * </p>
     *
     * @param id  知识库 ID
     * @param dto 知识库请求参数
     * @return 更新后的知识库信息
     */
    KnowledgeVO update(Long id, KnowledgeDTO dto);

    /**
     * 删除知识库
     * <p>
     * 逻辑删除知识库，同时删除文件存储目录。
     * </p>
     *
     * @param id 知识库 ID
     */
    void delete(Long id);

    /**
     * 查询全部知识库
     * <p>
     * 不分页，仅返回已启用的知识库，供下拉选择等场景使用。
     * </p>
     *
     * @return 已启用的知识库列表
     */
    List<KnowledgeVO> queryAll();

    /**
     * 获取知识库文件列表
     * <p>
     * 查询指定知识库目录下的所有文件信息，包括文件名、大小、MIME 类型等。
     * </p>
     *
     * @param knowledgeId 知识库 ID
     * @return 文件信息列表
     */
    List<KnowledgeFileVO> listFiles(Long knowledgeId);

    /**
     * 上传文件到知识库
     * <p>
     * 将文件上传到指定知识库的存储目录，自动生成带时间戳和随机后缀的文件名，
     * 并更新知识库的文档数量。
     * </p>
     *
     * @param knowledgeId 知识库 ID
     * @param file        要上传的文件
     * @return 上传后的文件信息
     */
    KnowledgeFileVO uploadFile(Long knowledgeId, MultipartFile file);

    /**
     * 删除知识库文件
     * <p>
     * 从指定知识库的存储目录中删除文件，并更新知识库的文档数量。
     * </p>
     *
     * @param knowledgeId 知识库 ID
     * @param fileId      文件 ID（存储文件名）
     */
    void deleteFile(Long knowledgeId, String fileId);

    /**
     * 获取知识库文件资源（用于下载）
     * <p>
     * 返回指定知识库文件的 Resource 对象，包含文件二进制流和 MIME 类型信息，
     * 用于浏览器下载文件。
     * </p>
     *
     * @param knowledgeId 知识库 ID
     * @param fileId      文件 ID（存储文件名）
     * @return 文件资源对象
     */
    Resource getFileResource(Long knowledgeId, String fileId);
}