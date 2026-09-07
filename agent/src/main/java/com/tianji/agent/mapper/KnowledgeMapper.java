package com.tianji.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.agent.model.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库管理持久层 Mapper
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper 接口，自动获得 knowledge 表的 CRUD 能力，
 * 无需编写 XML 或 SQL 即可完成基本的数据库操作。
 * </p>
 */
@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {
}