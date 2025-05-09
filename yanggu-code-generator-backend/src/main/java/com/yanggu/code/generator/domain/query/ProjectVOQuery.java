package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.vo.ProjectVO;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 项目VO查询实体类
 */
@Data
@Schema(description = "项目VO查询实体类")
@EqualsAndHashCode(callSuper = true)
public class ProjectVOQuery extends PageQuery<ProjectVO> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}