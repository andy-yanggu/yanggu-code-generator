package com.yanggu.code.generator.domain.bo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 模板组BO实体类
 */
@Data
public class TemplateGroupBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板组名称
     */
    private String groupName;

    /**
     * 模板组类型（0-项目，1-表，2-枚举）
     */
    private Integer type;

    /**
     * 模板组描述
     */
    private String groupDesc;

    /**
     * 模板列表
     */
    private List<TemplateBO> templateList;

}