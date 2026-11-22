package com.yanggu.code.generator.service.impl;

import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.date.DateUtil;
import cn.hutool.v7.core.io.IoUtil;
import cn.hutool.v7.core.text.StrUtil;
import cn.hutool.v7.core.util.CharsetUtil;
import cn.hutool.v7.http.meta.HttpHeaderUtil;
import cn.hutool.v7.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.bo.TemplateGroupPropertyBO;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyDTO;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.entity.TemplateEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyQuery;
import com.yanggu.code.generator.domain.vo.TemplateGroupPropertyVO;
import com.yanggu.code.generator.mapper.TemplateGroupPropertyMapper;
import com.yanggu.code.generator.mapstruct.TemplateGroupPropertyMapstruct;
import com.yanggu.code.generator.service.EnumService;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TableService;
import com.yanggu.code.generator.service.TemplateGroupPropertyService;
import com.yanggu.code.generator.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.hutool.v7.core.date.DateFormatPool.PURE_DATETIME_PATTERN;
import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

/**
 * 模板组属性Service实现类
 */
@Service
public class TemplateGroupPropertyServiceImpl extends ServiceImpl<TemplateGroupPropertyMapper, TemplateGroupPropertyEntity> implements TemplateGroupPropertyService {

    @Autowired
    private TemplateGroupPropertyMapper templateGroupPropertyMapper;

    @Autowired
    private TemplateGroupPropertyMapstruct templateGroupPropertyMapstruct;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TableService tableService;

    @Autowired
    private EnumService enumService;

    @Autowired
    private TemplateService templateService;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateGroupPropertyEntity add(TemplateGroupPropertyDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        TemplateGroupPropertyEntity entity = templateGroupPropertyMapstruct.dtoToEntity(dto);
        templateGroupPropertyMapper.insert(entity);
        return entity;
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TemplateGroupPropertyDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        TemplateGroupPropertyEntity formEntity = templateGroupPropertyMapstruct.dtoToEntity(dto);
        TemplateGroupPropertyEntity dbEntity = selectById(dto.getId());
        templateGroupPropertyMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        deleteList(List.of(id));
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteList(List<Long> idList) {
        List<TemplateGroupPropertyEntity> dbEntityList = templateGroupPropertyMapper.selectByIds(idList);
        //删除校验
        checkReference(dbEntityList);
        //关联删除
        templateGroupPropertyMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public TemplateGroupPropertyVO detail(Long id) {
        TemplateGroupPropertyEntity dbEntity = selectById(id);
        return templateGroupPropertyMapstruct.entityToVO(dbEntity);
    }

    /**
     * 详情列表
     */
    @Override
    public List<TemplateGroupPropertyVO> detailList(List<Long> idList) {
        List<TemplateGroupPropertyEntity> entityList = templateGroupPropertyMapper.selectByIds(idList);
        return templateGroupPropertyMapstruct.entityToVO(entityList);
    }

    /**
     * 分页（完整VO）
     */
    @Override
    public PageVO<TemplateGroupPropertyVO> page(TemplateGroupPropertyQuery query) {
        //无JOIN需求，使用QueryWrapper
        Page<TemplateGroupPropertyEntity> page = query.toPage();
        templateGroupPropertyMapper.selectPage(page, buildQueryWrapper(query));
        return templateGroupPropertyMapstruct.entityToPageVO(page);
    }

    /**
     * 列表（完整VO）
     */
    @Override
    public List<TemplateGroupPropertyVO> list(TemplateGroupPropertyQuery query) {
        //无JOIN需求，使用QueryWrapper
        List<TemplateGroupPropertyEntity> entityList = templateGroupPropertyMapper.selectList(buildQueryWrapper(query));
        return templateGroupPropertyMapstruct.entityToVO(entityList);
    }

    @Override
    public ResponseEntity<byte[]> export(List<Long> idList) {
        List<TemplateGroupPropertyEntity> list = idList.stream()
                .map(this::selectById)
                .toList();

        //转换成导出对象
        List<TemplateGroupPropertyBO> boList = templateGroupPropertyMapstruct.entityToBO(list);

        String jsonStr = JSONUtil.toJsonStr(boList);

        String fileName = "template_group_property_" + DateUtil.format(LocalDateTime.now(), PURE_DATETIME_PATTERN) + ".json";
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, HttpHeaderUtil.createAttachmentDisposition(fileName, CharsetUtil.UTF_8))
                .contentType(APPLICATION_OCTET_STREAM)
                .body(jsonStr.getBytes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTemplateGroupProperty(MultipartFile file, Long templateGroupId) throws IOException {
        String data = IoUtil.read(file.getInputStream(), CharsetUtil.UTF_8);
        if (StrUtil.isBlank(data)) {
            throw new BusinessException("导入数据不能为空");
        }
        List<TemplateGroupPropertyBO> list = JSONUtil.toList(data, TemplateGroupPropertyBO.class);
        if (CollUtil.isEmpty(list)) {
            throw new BusinessException("导入数据不能为空");
        }

        list.forEach(bo -> {
            TemplateGroupPropertyDTO dto = templateGroupPropertyMapstruct.boToDTO(bo);
            dto.setTemplateGroupId(templateGroupId);
            add(dto);
        });
    }


    /**
     * ID查询
     */
    private TemplateGroupPropertyEntity selectById(Long id) {
        TemplateGroupPropertyEntity entity = templateGroupPropertyMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "模板组属性", id);
        }
        return entity;
    }

    /**
     * 唯一性校验
     */
    private void checkUnique(TemplateGroupPropertyDTO dto) {
        LambdaQueryWrapper<TemplateGroupPropertyEntity> wrapper = Wrappers.lambdaQuery(TemplateGroupPropertyEntity.class);

        wrapper.ne(Objects.nonNull(dto.getId()), TemplateGroupPropertyEntity::getId, dto.getId());
        wrapper.eq(TemplateGroupPropertyEntity::getTemplateGroupId, dto.getTemplateGroupId());
        wrapper.and(temp -> temp
                .eq(TemplateGroupPropertyEntity::getPropTitle, dto.getPropTitle())
                .or()
                .eq(TemplateGroupPropertyEntity::getPropKey, dto.getPropKey())
        );

        boolean exists = templateGroupPropertyMapper.exists(wrapper);
        if (exists) {
            throw new BusinessException("模板组属性标题或者属性键已存在");
        }
    }

    /**
     * 校验能否被删除：覆盖引用 + 模板隐式引用检测。
     * 属性 (templateGroupId=G, propKey=K) 被引用，当且仅当某个绑定到组 G 的产品
     * （项目/表/枚举）在其 templateGroupPropertyData 中显式存过 key=K，
     * 或组内模板内容以 ${xxxTemplatePropertyData.propKey} 形式引用该属性。
     * 此时删除属性会让用户配置值变孤儿、或让模板取到空值，故拦截并聚合报错。
     */
    private void checkReference(List<TemplateGroupPropertyEntity> dbEntityList) {
        if (CollUtil.isEmpty(dbEntityList)) {
            return;
        }

        // 待删除属性按模板组归类：groupId -> (propKey -> propTitle)，propTitle 用于生成可读报错
        Map<Long, Map<String, String>> deleteKeyMap = new HashMap<>();
        for (TemplateGroupPropertyEntity entity : dbEntityList) {
            deleteKeyMap.computeIfAbsent(entity.getTemplateGroupId(), k -> new HashMap<>())
                    .put(entity.getPropKey(), entity.getPropTitle());
        }
        Set<Long> groupIdSet = deleteKeyMap.keySet();
        List<String> referenceList = new ArrayList<>();

        // 1. 项目维度：project.projectTemplateGroupId 命中，直接看项目自身配置
        projectService.list(Wrappers.<ProjectEntity>lambdaQuery()
                        .in(ProjectEntity::getProjectTemplateGroupId, groupIdSet))
                .forEach(project -> collectReference(
                        deleteKeyMap.get(project.getProjectTemplateGroupId()),
                        project.getTemplateGroupPropertyData(),
                        (propKey, propTitle) -> referenceList.add(
                                StrUtil.format("属性[{}]已被项目[{}]配置引用", propTitle, project.getProjectName()))));

        // 2. 表维度：表不直接绑定组，需经所属项目的 tableTemplateGroupId 命中，再看表自身配置
        Map<Long, Long> projectTableGroupIdMap = projectService.list(Wrappers.<ProjectEntity>lambdaQuery()
                        .in(ProjectEntity::getTableTemplateGroupId, groupIdSet))
                .stream()
                .collect(Collectors.toMap(ProjectEntity::getId, ProjectEntity::getTableTemplateGroupId));
        if (CollUtil.isNotEmpty(projectTableGroupIdMap)) {
            tableService.list(Wrappers.<TableEntity>lambdaQuery()
                            .in(TableEntity::getProjectId, projectTableGroupIdMap.keySet()))
                    .forEach(table -> collectReference(
                            deleteKeyMap.get(projectTableGroupIdMap.get(table.getProjectId())),
                            table.getTemplateGroupPropertyData(),
                            (propKey, propTitle) -> referenceList.add(
                                    StrUtil.format("属性[{}]已被表[{}]配置引用", propTitle, table.getTableName()))));
        }

        // 3. 枚举维度：同表维度，经所属项目的 enumTemplateGroupId 命中
        Map<Long, Long> projectEnumGroupIdMap = projectService.list(Wrappers.<ProjectEntity>lambdaQuery()
                        .in(ProjectEntity::getEnumTemplateGroupId, groupIdSet))
                .stream()
                .collect(Collectors.toMap(ProjectEntity::getId, ProjectEntity::getEnumTemplateGroupId));
        if (CollUtil.isNotEmpty(projectEnumGroupIdMap)) {
            enumService.list(Wrappers.<EnumEntity>lambdaQuery()
                            .in(EnumEntity::getProjectId, projectEnumGroupIdMap.keySet()))
                    .forEach(enumEntity -> collectReference(
                            deleteKeyMap.get(projectEnumGroupIdMap.get(enumEntity.getProjectId())),
                            enumEntity.getTemplateGroupPropertyData(),
                            (propKey, propTitle) -> referenceList.add(
                                    StrUtil.format("属性[{}]已被枚举[{}]配置引用", propTitle, enumEntity.getEnumName()))));
        }

        // 4. 隐式引用：组内模板内容以 ${xxxTemplatePropertyData.propKey} / ['propKey'] 形式引用该属性
        List<TemplateEntity> templateList = templateService.list(Wrappers.<TemplateEntity>lambdaQuery()
                .in(TemplateEntity::getTemplateGroupId, groupIdSet));
        if (CollUtil.isNotEmpty(templateList)) {
            // 每个待删 propKey 预编译访问正则，避免逐模板重复编译
            Map<String, Pattern> keyPatternMap = new HashMap<>();
            deleteKeyMap.values().forEach(keyTitleMap -> keyTitleMap.keySet()
                    .forEach(propKey -> keyPatternMap.computeIfAbsent(propKey,
                            TemplateGroupPropertyServiceImpl::buildPropertyAccessPattern)));

            templateList.stream()
                    .filter(template -> StrUtil.isNotBlank(template.getTemplateContent()))
                    .forEach(template -> {
                        Map<String, String> keyTitleMap = deleteKeyMap.get(template.getTemplateGroupId());
                        if (keyTitleMap == null) {
                            return;
                        }
                        keyTitleMap.forEach((propKey, propTitle) -> {
                            if (keyPatternMap.get(propKey).matcher(template.getTemplateContent()).find()) {
                                referenceList.add(StrUtil.format("属性[{}]已被模板[{}]内容引用",
                                        propTitle, template.getTemplateName()));
                            }
                        });
                    });
        }

        if (CollUtil.isNotEmpty(referenceList)) {
            throw new BusinessException("模板组属性已被引用，无法删除：" + String.join("；", referenceList));
        }
    }

    /**
     * 命中检测：产品在给定模板组下显式存过待删除的 propKey，即视为覆盖引用，逐条回调收集
     */
    private void collectReference(Map<String, String> keyTitleMap, Map<String, Object> data,
                                  BiConsumer<String, String> onReference) {
        if (CollUtil.isEmpty(keyTitleMap) || CollUtil.isEmpty(data)) {
            return;
        }
        keyTitleMap.forEach((propKey, propTitle) -> {
            if (data.containsKey(propKey)) {
                onReference.accept(propKey, propTitle);
            }
        });
    }

    /**
     * 构建属性访问匹配正则：xxxTemplatePropertyData 后紧跟点/方括号/引号/空白，再命中完整 propKey。
     * 邻接约束 + propKey 词边界（\b）可将误报压到极低，且二进制模板为 data URL 不含该字面量，天然排除。
     */
    private static Pattern buildPropertyAccessPattern(String propKey) {
        String regex = "TemplatePropertyData[\\s.\\['\"]*" + Pattern.quote(propKey) + "\\b";
        return Pattern.compile(regex);
    }

    private LambdaQueryWrapper<TemplateGroupPropertyEntity> buildQueryWrapper(TemplateGroupPropertyQuery query) {
        LambdaQueryWrapper<TemplateGroupPropertyEntity> wrapper = Wrappers.lambdaQuery(TemplateGroupPropertyEntity.class);

        //过滤字段
        wrapper.eq(MybatisUtil.isNotEmpty(query.getTemplateGroupId()), TemplateGroupPropertyEntity::getTemplateGroupId, query.getTemplateGroupId());
        wrapper.like(MybatisUtil.isNotEmpty(query.getPropTitle()), TemplateGroupPropertyEntity::getPropTitle, query.getPropTitle());
        wrapper.like(MybatisUtil.isNotEmpty(query.getPropKey()), TemplateGroupPropertyEntity::getPropKey, query.getPropKey());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
    }

}
