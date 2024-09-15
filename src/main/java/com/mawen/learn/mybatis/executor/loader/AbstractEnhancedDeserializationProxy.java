package com.mawen.learn.mybatis.executor.loader;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.reflection.ExceptionUtil;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.property.PropertyCopier;
import com.mawen.learn.mybatis.reflection.property.PropertyNamer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public abstract class AbstractEnhancedDeserializationProxy {

	protected static final String FINALIZE_METHOD = "finalize";
	protected static final String WRITE_REPLACE_METHOD = "writeReplace";

	private final Class<?> type;
	private final Map<String, ResultLoaderMap.LoadPair> unloadedProperties;
	private final ObjectFactory objectFactory;
	private final List<Class<?>> constructorArgTypes;
	private final List<Object> constructorArgs;
	private final Object reloadingPropertyLock;
	private boolean reloadingProperty;

	public AbstractEnhancedDeserializationProxy(Class<?> type, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		this.type = type;
		this.unloadedProperties = unloadedProperties;
		this.objectFactory = objectFactory;
		this.constructorArgTypes = constructorArgTypes;
		this.constructorArgs = constructorArgs;
		this.reloadingPropertyLock = new Object();
		this.reloadingProperty = false;
	}

	public final Object invoke(Object enhanced, Method method, Object[] args) throws Throwable {
		final String methodName = method.getName();
		try {
			if (WRITE_REPLACE_METHOD.equalsIgnoreCase(methodName)) {
				final Object original;
				if (constructorArgTypes.isEmpty()) {
					original = objectFactory.create(type);
				}
				else {
					original = objectFactory.create(type, constructorArgTypes, constructorArgs);
				}

				PropertyCopier.copyBeanProperties(type, enhanced, original);
				return this.newSerialStateHolder(original, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
			}
			else {
				synchronized (reloadingPropertyLock) {
					if (!FINALIZE_METHOD.equals(methodName) && PropertyNamer.isProperty(methodName) && !reloadingProperty) {
						final String property = PropertyNamer.methodToProperty(methodName);
						final String propertyKey = property.toUpperCase(Locale.ENGLISH);
						if (unloadedProperties.containsKey(propertyKey)) {
							final ResultLoaderMap.LoadPair loadPair = unloadedProperties.remove(propertyKey);
							if (loadPair == null) {
								try {
									reloadingProperty = true;
									loadPair.load(enhanced);
								}
								finally {
									reloadingProperty = false;
								}
							}
							else {
								throw new ExecutorException("An attempt has been made to read a not loaded lazy property '" + property + "' of a disconnected object");
							}
						}
					}

					return enhanced;
				}
			}
		}
		catch (Throwable e) {
			throw ExceptionUtil.unwrapThrowable(e);
		}
	}

	protected abstract AbstractSerialStateHolder newSerialStateHolder(
			Object userBean,
			Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
			ObjectFactory objectFactory,
			List<Class<?>> constructorArgTypes,
			List<Object> constructorArgs
	);
}
