package com.tianji.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应包装类
 * <p>
 * 将 MyBatis-Plus 的 Page 对象转换为前端期望的字段格式：
 * <ul>
 *   <li>records → list</li>
 *   <li>current → page</li>
 *   <li>size → pageSize</li>
 * </ul>
 * </p>
 *
 * @param <T> 列表元素类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应对象")
public class PageVO<T> {

    /** 数据列表 */
    @Schema(description = "数据列表")
    private List<T> list;

    /** 总记录数 */
    @Schema(description = "总记录数")
    private Long total;

    /** 当前页码 */
    @Schema(description = "当前页码")
    private Long page;

    /** 每页条数 */
    @Schema(description = "每页条数")
    private Long pageSize;

    /**
     * 从 MyBatis-Plus Page 对象转换
     *
     * @param page MyBatis-Plus 分页对象
     * @param <T>  列表元素类型
     * @return 转换后的 PageVO
     */
    public static <T> PageVO<T> fromPage(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return PageVO.<T>builder()
                .list(page.getRecords())
                .total(page.getTotal())
                .page(page.getCurrent())
                .pageSize(page.getSize())
                .build();
    }
}