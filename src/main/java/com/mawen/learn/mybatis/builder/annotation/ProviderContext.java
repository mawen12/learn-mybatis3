package com.mawen.learn.mybatis.builder.annotation;

import java.lang.reflect.Method;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public final class ProviderContext {

	private final Class<?> mapperType;
	private final Method mapperMethod;
	private final String databaseId;

	ProviderContext(Class<?> mapperType, Method mapperMethod, String databaseId) {
		this.mapperType = mapperType;
		this.mapperMethod = mapperMethod;
		this.databaseId = databaseId;
	}

	public Class<?> getMapperType() {
		return mapperType;
	}

	public Method getMapperMethod() {
		return mapperMethod;
	}

	public String getDatabaseId() {
		return databaseId;
	}
}
