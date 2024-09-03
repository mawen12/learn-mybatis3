package com.mawen.learn.mybatis.reflection.factory;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public interface ObjectFactory {

	default void setProperties(Properties properties) {
		// NOP
	}

	<T> T create(Class<T> type);

	<T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

	<T> boolean isCollection(Class<T> type);
}
