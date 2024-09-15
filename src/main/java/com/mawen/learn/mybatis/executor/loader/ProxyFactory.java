package com.mawen.learn.mybatis.executor.loader;

import java.util.List;
import java.util.Properties;

import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public interface ProxyFactory {

	default void setProperties(Properties properties) {
		// NOP
	}

	Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);
}
