package com.mawen.learn.mybatis.executor.loader.javassist;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.executor.loader.AbstractEnhancedDeserializationProxy;
import com.mawen.learn.mybatis.executor.loader.AbstractSerialStateHolder;
import com.mawen.learn.mybatis.executor.loader.ProxyFactory;
import com.mawen.learn.mybatis.executor.loader.ResultLoaderMap;
import com.mawen.learn.mybatis.executor.loader.WriteReplaceInterface;
import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import com.mawen.learn.mybatis.reflection.ExceptionUtil;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.property.PropertyCopier;
import com.mawen.learn.mybatis.reflection.property.PropertyNamer;
import com.mawen.learn.mybatis.session.Configuration;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public class JavassistProxyFactory implements ProxyFactory {

	private static final String FINALIZE_METHOD = "finalize";
	private static final String WRITE_REPLACE_METHOD = "writeReplace";

	public JavassistProxyFactory() {
		try {
			Resources.classForName("javassist.util.proxy.ProxyFactory");
		}
		catch (Throwable e) {
			throw new IllegalStateException("Cannot enable lazy loading because Javassist is not available. Add Javassist to your classpath.", e);
		}
	}

	@Override
	public Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		return EnhancedResultObjectProxyImpl.createProxy(target, lazyLoader, configuration, objectFactory, constructorArgTypes, constructorArgs);
	}

	public Object createDeserializationProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		return EnhancedDeserializationProxyImpl.createProxy(target, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
	}

	static Object createProxy(Class<?> type, MethodHandler callback, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {

		javassist.util.proxy.ProxyFactory enhancer = new javassist.util.proxy.ProxyFactory();
		enhancer.setSuperclass(type);

		try {
			type.getDeclaredMethod(WRITE_REPLACE_METHOD);

			if (LogHolder.log.isDebugEnabled()) {
				LogHolder.log.debug(WRITE_REPLACE_METHOD + " method was found on bean " + type + ", make sure it returns this");
			}
		}
		catch (NoSuchMethodException e) {
			enhancer.setInterfaces(new Class[] {WriteReplaceInterface.class});
		}
		catch (SecurityException ignored) {
		}

		Object enhanced;
		Class[] typesArray = constructorArgTypes.toArray(new Class[constructorArgTypes.size()]);
		Object[] valuesArray = constructorArgs.toArray(new Object[constructorArgs.size()]);
		try {
			enhanced = enhancer.create(typesArray, valuesArray);
		}
		catch (Exception e) {
			throw new ExecutorException("Error creating lazy proxy. Cause: " + e, e);
		}

		((Proxy)enhanced).setHandler(callback);
		return enhanced;
	}

	private static class EnhancedResultObjectProxyImpl implements MethodHandler {

		private final Class<?> type;
		private final ResultLoaderMap lazyLoader;
		private final boolean aggressive;
		private final Set<String> lazyLoadTriggerMethods;
		private final ObjectFactory objectFactory;
		private final List<Class<?>> constructorArgTypes;
		private final List<Object> constructorArgs;

		private EnhancedResultObjectProxyImpl(Class<?> type, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			this.type = type;
			this.lazyLoader = lazyLoader;
			this.aggressive = configuration.isAggressiveLazyLoading();
			this.lazyLoadTriggerMethods = configuration.getLazyLoadTriggerMethods();
			this.objectFactory = objectFactory;
			this.constructorArgTypes = constructorArgTypes;
			this.constructorArgs = constructorArgs;
		}

		public static Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			Class<?> type = target.getClass();
			EnhancedResultObjectProxyImpl callback = new EnhancedResultObjectProxyImpl(type, lazyLoader, configuration, objectFactory, constructorArgTypes, constructorArgs);
			Object enhanced = JavassistProxyFactory.createProxy(type, callback, constructorArgTypes, constructorArgs);
			PropertyCopier.copyBeanProperties(type, target, enhanced);
			return enhanced;
		}

		@Override
		public Object invoke(Object enhanced, Method method, Method methodProxy, Object[] args) throws Throwable {
			String methodName = method.getName();
			try {
				synchronized (lazyLoader) {
					if (WRITE_REPLACE_METHOD.equals(methodName)) {
						Object original;
						if (constructorArgTypes.isEmpty()) {
							original = objectFactory.create(type);
						}
						else {
							original = objectFactory.create(type, constructorArgTypes, constructorArgs);
						}

						PropertyCopier.copyBeanProperties(type, enhanced, original);
						if (lazyLoader.size() > 0) {
							return new JavassistSerialStateHolder(original, lazyLoader.getProperties(), objectFactory, constructorArgTypes, constructorArgs);
						}
						else {
							return original;
						}
					}
					else {
						if (lazyLoader.size() > 0 && !FINALIZE_METHOD.equals(methodName)) {
							if (aggressive || lazyLoadTriggerMethods.contains(methodName)) {
								lazyLoader.loadAll();
							}
							else if (PropertyNamer.isSetter(methodName)) {
								String property = PropertyNamer.methodToProperty(methodName);
								lazyLoader.remove(property);
							}
							else if (PropertyNamer.isGetter(methodName)) {
								String property = PropertyNamer.methodToProperty(methodName);
								if (lazyLoader.hasLoader(property)) {
									lazyLoader.load(property);
								}
							}
						}
					}
				}
				return methodProxy.invoke(enhanced, args);
			}
			catch (Throwable e) {
				throw ExceptionUtil.unwrapThrowable(e);
			}
		}
	}

	private static class EnhancedDeserializationProxyImpl extends AbstractEnhancedDeserializationProxy implements MethodHandler {

		private EnhancedDeserializationProxyImpl(Class<?> type, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory,
		                                         List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			super(type, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
		}

		public static Object createProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadProperties, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			final Class<?> type = target.getClass();
			EnhancedDeserializationProxyImpl callback = new EnhancedDeserializationProxyImpl(type, unloadProperties, objectFactory, constructorArgTypes, constructorArgs);
			Object enhanced = JavassistProxyFactory.createProxy(type, callback, constructorArgTypes, constructorArgs);
			PropertyCopier.copyBeanProperties(type, target, enhanced);
			return enhanced;
		}

		@Override
		public Object invoke(Object enhanced, Method method, Method methodProxy, Object[] args) throws Throwable {
			final Object o = super.invoke(enhanced, method, args);
			return o instanceof AbstractSerialStateHolder ? o : methodProxy.invoke(o, args);
		}

		@Override
		protected AbstractSerialStateHolder newSerialStateHolder(Object userBean, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			return new JavassistSerialStateHolder(userBean, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
		}
	}

	private static class LogHolder {
		private static final Log log = LogFactory.getLog(JavassistProxyFactory.class);
	}
}
