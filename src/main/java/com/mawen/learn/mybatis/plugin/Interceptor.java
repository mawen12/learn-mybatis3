package com.mawen.learn.mybatis.plugin;

import java.util.Properties;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public interface Interceptor {

	Object interceptor(Invocation invocation) throws Throwable;

	default Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	default void setProperties(Properties properties) {
		// NOP
	}
}
