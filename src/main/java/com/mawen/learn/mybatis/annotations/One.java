package com.mawen.learn.mybatis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mawen.learn.mybatis.mapping.FetchType;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface One {

	String columnPrefix() default "";

	String resultMap() default "";

	String select() default "";

	FetchType fetchType() default FetchType.DEFAULT;
}
