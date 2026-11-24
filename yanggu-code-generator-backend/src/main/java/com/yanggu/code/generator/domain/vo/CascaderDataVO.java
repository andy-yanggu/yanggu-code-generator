package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "级联选择器数据VO实体类")
public class CascaderDataVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "标签")
    private String label;

    @Schema(description = "值")
    private String value;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "生成器类型")
    private Integer generatorType;

    @Schema(description = "子级数据")
    private List<CascaderDataVO> children;

    /**
     * 装配级联节点：前端按 '_' 反解 value 取 ID，value 前缀由 type 拼出，两者天然同源，不得手写第二处
     *
     * @param id            产物 ID
     * @param label         级联显示名（项目名 / 表名 / 枚举名）
     * @param description   级联描述（项目描述 / 表注释 / 枚举描述）
     * @param type          产物类型小写名：project / table / enum
     * @param generatorType 生成器类型，恒取所属项目
     */
    public static CascaderDataVO of(Long id, String label, String description, String type, Integer generatorType) {
        CascaderDataVO cascaderDataVO = new CascaderDataVO();
        cascaderDataVO.setId(id);
        cascaderDataVO.setLabel(label);
        cascaderDataVO.setDescription(description);
        cascaderDataVO.setType(type);
        cascaderDataVO.setValue(type + "_" + id);
        cascaderDataVO.setGeneratorType(generatorType);
        return cascaderDataVO;
    }

}
