package com.yanggu.code.generator.service.support;

import cn.hutool.v7.core.util.EnumUtil;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 产物处理器路由表：按模板组类型注册并分发 ProductGenerator 策略。
 * 从 GeneratorServiceImpl 内部私有路由表抽出，供生成链路与模板组链路共用同一份策略集，
 * 非法类型在此一次性给出明确业务报错，避免各处 switch(null) NPE 与散落判空
 */
@Component
public class ProductGenerators {

    // 故意用字段注入 + @PostConstruct 而非构造注入：存在回边
    // TemplateGroupService -> ProductGenerators -> ProductGenerator -> PreviewBuilder -> TemplateGroupService，
    // 构造注入时本组件实例在构造完成前无法提供早期引用，该环会随 Bean 创建顺序随机启动失败
    @Autowired
    private List<ProductGenerator> productGenerators;

    private Map<TemplateGroupTypeEnum, ProductGenerator> generatorMap;

    @PostConstruct
    void initGeneratorMap() {
        generatorMap = new EnumMap<>(TemplateGroupTypeEnum.class);
        productGenerators.forEach(generator -> generatorMap.put(generator.type(), generator));
    }

    /**
     * 按类型获取产物处理器
     */
    public ProductGenerator get(TemplateGroupTypeEnum type) {
        ProductGenerator generator = type == null ? null : generatorMap.get(type);
        if (generator == null) {
            throw new BusinessException("不支持的模板组类型: {}", type);
        }
        return generator;
    }

    /**
     * 按类型编码获取产物处理器：EnumUtil.getBy 未命中返回 null，交给 switch 会直接 NPE，故此处显式判空
     */
    public ProductGenerator getByCode(Integer code) {
        TemplateGroupTypeEnum type = EnumUtil.getBy(TemplateGroupTypeEnum::getCode, code);
        if (type == null) {
            throw new BusinessException("模板组类型错误, code: {}", code);
        }
        return get(type);
    }

}
