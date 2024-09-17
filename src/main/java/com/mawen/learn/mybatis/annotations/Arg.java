package com.mawen.learn.mybatis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.UnknownTypeHandler;
import com.sun.org.apache.bcel.internal.util.Args;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ConstructorArgs.class)
public @interface Arg {

	boolean id() default false;

	String column() default "";

	Class<?> javaType() default void.class;

	JdbcType jdbcType() default JdbcType.UNDEFINED;

	Class<? extends TypeHandler> typeHandler() default UnknownTypeHandler.class;

	String select() default "";

	String resultMap() default "";

	String name() default "";

	String columnPrefix() default "";
}
