package com.mawen.learn.mybatis.executor.loader.javassist;

import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.executor.loader.AbstractSerialStateHolder;
import com.mawen.learn.mybatis.executor.loader.ResultLoaderMap;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public class JavassistSerialStateHolder extends AbstractSerialStateHolder {

	public JavassistSerialStateHolder() {}

	public JavassistSerialStateHolder(
			final Object userBean,
			final Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
			final ObjectFactory objectFactory,
			List<Class<?>> constructorArgTypes,
			List<Object> constructorArgs) {
		super(userBean, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
	}

	@Override
	protected Object createDeserializationProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		return null;
	}
}
