<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${projectPackage}.${projectNameDot}.common.mybatis.mapper.BaseMapperPlus">

    <!-- 排序SQL -->
    <sql id="orderBySQL">
        <bind name="columnPrefix" value="''"/>
        <include refid="${projectPackage}.${projectNameDot}.common.mybatis.mapper.BaseMapperPlus.prefixOrderBySQL"/>
    </sql>

    <!-- 排序SQL（带前缀） -->
    <sql id="prefixOrderBySQL">
        <if test="@${projectPackage}.${projectNameDot}.common.mybatis.util.MybatisUtil@isNotEmpty(query, 'orderItemList')">
            ORDER BY
            <foreach collection="query.orderItemList" item="order" separator=",">
                <if test="@${projectPackage}.${projectNameDot}.common.mybatis.util.MybatisUtil@isNotEmpty(columnPrefix)">
                    ${columnPrefix}.
                </if>
                <#noparse>${columnPrefix}.${@</#noparse>${projectPackage}.${projectNameDot}.common.mybatis.util.MybatisUtil@toUnderlineCase(order.column)} <if test="order.asc">ASC</if><if test="!order.asc">DESC</if>
            </foreach>
        </if>
    </sql>

</mapper>
