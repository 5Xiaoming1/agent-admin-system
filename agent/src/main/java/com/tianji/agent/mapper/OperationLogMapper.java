package com.tianji.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.agent.model.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志持久层 Mapper
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper 接口，自动获得 operation_log 表的 CRUD 能力，
 * 无需编写 XML 或 SQL 即可完成基本的数据库操作。
 * </p>
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}