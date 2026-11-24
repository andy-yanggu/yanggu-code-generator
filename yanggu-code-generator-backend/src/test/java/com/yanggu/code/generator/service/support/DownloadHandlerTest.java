package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DownloadHandlerTest {

    private final DownloadHandler downloadHandler = new DownloadHandler();

    @Test
    void emptyArtifactFailsIdenticallyOnZipAndLocalOutput() {
        BusinessException zipException = assertThrows(BusinessException.class,
                () -> downloadHandler.downloadZip("demo", List.of()));
        BusinessException localException = assertThrows(BusinessException.class,
                () -> downloadHandler.downloadLocal(List.of()));

        // 本地写出曾只 log.warn 后静默返回，调用方拿到 200 仍提示“已生成到服务器本地”，实际零文件：
        // 两个输出的空产出必须同文案，回归时不得只改一边
        assertEquals(zipException.getMessage(), localException.getMessage());
        assertEquals(500, localException.getCode().intValue());
    }
}
