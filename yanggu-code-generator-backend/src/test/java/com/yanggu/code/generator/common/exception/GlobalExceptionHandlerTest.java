package com.yanggu.code.generator.common.exception;

import com.yanggu.code.generator.common.response.ResultEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 参数类型不匹配链路的回归测试
 * <p>用 standalone MockMvc 起一个最小 MVC 上下文，验证三件事：</p>
 * <ol>
 *   <li>MVC 在类型转换阶段抛出的确实是 {@code MethodArgumentTypeMismatchException}，而非落到别处；</li>
 *   <li>它命中 {@link GlobalExceptionHandler} 新增的专列处理器，而不是被 {@code RuntimeException}/{@code Exception} 兜底成 500；</li>
 *   <li>响应体只回显参数名与期望类型，不回显调用方传入的原始值。</li>
 * </ol>
 * 处理器匹配优先级只由框架决定，跑一遍才算守住；本测试不连数据库、不依赖 Spring Boot 上下文。
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        // advice 里的 request 是字段注入，standalone 模式不走容器，手动补齐避免日志行 NPE
        ReflectionTestUtils.setField(handler, "request", new MockHttpServletRequest());
        mockMvc = MockMvcBuilders.standaloneSetup(new TypeMismatchController())
                .setControllerAdvice(handler)
                .build();
    }

    @Test
    void stringForLongParamReturnsParamErrWithColumnName() throws Exception {
        mockMvc.perform(get("/gen/test/detail").param("id", "abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultEnum.PARAM_ERR.getCode()))
                .andExpect(jsonPath("$.message").value("参数[id]类型不正确，应为Long"));
    }

    /**
     * 原始值不可信：即使传入形似标记串的内容，也只能进日志，不能回显给调用方
     */
    @Test
    void invalidValueIsNotEchoedBackToClient() throws Exception {
        String dangerousValue = "<script>alert(1)</script>";

        mockMvc.perform(get("/gen/test/detail").param("id", dangerousValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultEnum.PARAM_ERR.getCode()))
                .andExpect(jsonPath("$.message").value(not(containsString(dangerousValue))));
    }

    /**
     * 仅用于触发参数类型转换失败的最小控制器
     */
    @RestController
    static class TypeMismatchController {

        @GetMapping("/gen/test/detail")
        public String detail(@RequestParam("id") Long id) {
            return "id=" + id;
        }
    }

}
