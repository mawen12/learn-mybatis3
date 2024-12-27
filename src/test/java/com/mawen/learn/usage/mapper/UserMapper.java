package com.mawen.learn.usage.mapper;

import com.mawen.learn.mybatis.annotations.Param;
import com.mawen.learn.usage.entity.User;

public interface UserMapper {

    User selectById(@Param("id") Long id);

    User selectByIdNonCached(@Param("id") Long id);
}
