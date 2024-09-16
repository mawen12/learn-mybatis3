package com.mawen.learn.mybatis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mawen.learn.mybatis.mapping.ResultSetType;
import com.mawen.learn.mybatis.mapping.StatementType;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Options.List.class)
public @interface Options {

	boolean useCache() default true;

	FlushCachePolicy flushCache() default FlushCachePolicy.DEFAULT;

	ResultSetType resultSetType() default ResultSetType.DEFAULT;

	StatementType statementType() default StatementType.PREPARED;

	int fetchSize() default -1;

	int timeout() default -1;

	boolean useGeneratedKeys() default false;

	String keyProperty() default "";

	String keyColumn() default "";

	String resultSets() default "";

	String databaseId() default "";

	enum FlushCachePolicy {

		DEFAULT,

		TRUE,

		FALSE;
	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface List {
		Options[] value();
	}
}
