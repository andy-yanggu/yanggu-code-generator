package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorQueryTest {

    private GeneratorQuery query(Integer type, List<Long> productIds) {
        GeneratorQuery q = new GeneratorQuery();
        q.setProductType(type);
        q.setProductIds(productIds);
        return q;
    }

    @Test
    void invalidProductTypeThrows() {
        assertThrows(BusinessException.class, () -> query(99, List.of(1L)).validate(false));
        assertThrows(BusinessException.class, () -> query(null, List.of(1L)).validate(false));
    }

    @Test
    void emptyProductIdsThrows() {
        assertThrows(BusinessException.class, () -> query(1, List.of()).validate(false));
        assertThrows(BusinessException.class, () -> query(1, null).validate(false));
    }

    @Test
    void projectRequiresExactlyOneProductId() {
        assertThrows(BusinessException.class, () -> query(0, List.of(1L, 2L)).validate(false));
        assertDoesNotThrow(() -> query(0, List.of(1L)).validate(false));
    }

    @Test
    void nonProjectWithProjectFilterThrows() {
        GeneratorQuery q = query(1, List.of(1L));
        q.setProjectFilter(new ProjectFilter());
        assertThrows(BusinessException.class, () -> q.validate(false));
    }

    @Test
    void projectWithProjectFilterPasses() {
        GeneratorQuery q = query(0, List.of(1L));
        ProjectFilter filter = new ProjectFilter();
        filter.setTableIds(List.of(201L));
        q.setProjectFilter(filter);
        assertDoesNotThrow(() -> q.validate(false));
    }

    @Test
    void singleModeRequiresOneProductAndOneTemplate() {
        GeneratorQuery q = query(1, List.of(1L, 2L));
        q.setTemplateIds(List.of(11L));
        assertThrows(BusinessException.class, () -> q.validate(true));
        GeneratorQuery q2 = query(1, List.of(1L));
        assertThrows(BusinessException.class, () -> q2.validate(true));
        GeneratorQuery q3 = query(1, List.of(1L));
        q3.setTemplateIds(List.of(11L));
        assertDoesNotThrow(() -> q3.validate(true));
    }

    @Test
    void batchModeAllowsEmptyTemplateIds() {
        assertDoesNotThrow(() -> query(2, List.of(1L, 2L)).validate(false));
    }
}