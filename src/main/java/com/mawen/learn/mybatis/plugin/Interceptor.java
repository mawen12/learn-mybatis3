package com.mawen.learn.mybatis.plugin;

import java.util.Properties;

/**
 * 插件接口，自定义的接口，可以在全局配置文件中<plugins></plugins>节点中配置起效。
 * 插件接口底层由{@link Plugin}生成代理对象，并且其仅支持基于JDK的动态代理。
 * 这就对Mapper提出了要求，即Mapper只能是接口，不能是抽象类和具体类。
 *
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
