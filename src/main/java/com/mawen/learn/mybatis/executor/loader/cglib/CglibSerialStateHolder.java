package com.mawen.learn.mybatis.executor.loader.cglib;

import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.executor.loader.AbstractSerialStateHolder;
import com.mawen.learn.mybatis.executor.loader.ResultLoaderMap;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/17
 */
public class CglibSerialStateHolder extends AbstractSerialStateHolder {

	private static final long serialVersionUID = 7758167628047669151L;

	public CglibSerialStateHolder() {
	}

	public CglibSerialStateHolder(final Object userBean,
	                              final Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
	                              final ObjectFactory objectFactory,
	                              List<Class<?>> constructorArgTypes,
	                              List<Object> constructorArgs) {
		super(userBean, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
	}

	@Override
	protected Object createDeserializationProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		return new CglibProxyFactory().createDeserializationProxy(target, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
	}
}
