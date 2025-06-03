package com.yanggu.code.generator.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GeneratorTypeEnum {

    ZIP(0, "zip压缩包"),
    LOCAL(1, "本地"),
    ;

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String description;

}
