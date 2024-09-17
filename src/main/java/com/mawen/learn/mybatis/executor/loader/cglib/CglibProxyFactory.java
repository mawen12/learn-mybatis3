package com.mawen.learn.mybatis.executor.loader.cglib;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/17
 */
public class CglibProxyFactory implements ProxyFactory {

	private static final String FINALIZE_METHOD = "finalize";
	private static final String WRITE_REPLACE_METHOD = "writeReplace";

	public CglibProxyFactory() {
		try {
			Resources.classForName("net.sf.cglib.proxy.Enhancer");
		}
		catch (Throwable e) {
			throw new IllegalStateException("Cannot enable lazy loading because CGLIB is not available. Add CGLIB to your classpath.", e);
		}
	}

	@Override
	public Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		return EnhancedResultObjectProxyImpl.createProxy(target, lazyLoader, configuration, objectFactory, constructorArgTypes, constructorArgs);
	}

	public Object createDeserializationProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		return EnhancedDeserializationProxyImpl.createProxy(target, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
	}

	static Object createProxy(Class<?> type, Callback callback, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		LogHolder.log.warn("CglibProxyFactory is deprecated. Use another proxy factory implementation.");
		Enhancer enhancer = new Enhancer();
		enhancer.setCallback(callback);
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
		catch (SecurityException ignored) {}

		Object enhanced;
		if (constructorArgTypes.isEmpty()) {
			enhanced = enhancer.create();
		}
		else {
			Class[] typesArray = constructorArgTypes.toArray(new Class[constructorArgTypes.size()]);
			Object[] valuesArray = constructorArgs.toArray(new Object[constructorArgs.size()]);
			enhanced = enhancer.create(typesArray, valuesArray);
		}
		return enhanced;
	}

	private static class EnhancedResultObjectProxyImpl implements MethodInterceptor {

		private final Class<?> type;
		private final ResultLoaderMap lazyLoader;
		private final boolean aggressive;
		private final Set<String> lazyLoadTriggerMethods;
		private final ObjectFactory objectFactory;
		private final List<Class<?>> constructorArgTypes;
		private final List<Object> constructorArgs;

		public EnhancedResultObjectProxyImpl(Class<?> type, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			this.type = type;
			this.lazyLoader = lazyLoader;
			this.aggressive = configuration.isAggressiveLazyLoading();
			this.lazyLoadTriggerMethods = configuration.getLazyLoadingTriggerMethods();
			this.objectFactory = objectFactory;
			this.constructorArgTypes = constructorArgTypes;
			this.constructorArgs = constructorArgs;
		}

		public static Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			Class<?> type = target.getClass();
			EnhancedResultObjectProxyImpl callback = new EnhancedResultObjectProxyImpl(type, lazyLoader, configuration, objectFactory, constructorArgTypes, constructorArgs);
			Object enhanced = createProxy(type, callback, constructorArgTypes, constructorArgs);
			PropertyCopier.copyBeanProperties(type, target, enhanced);
			return enhanced;
		}

		@Override
		public Object intercept(Object enhanced, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			final String methodName = method.getName();
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
							return new CglibSerialStateHolder(original, lazyLoader.getProperties(), objectFactory, constructorArgTypes, constructorArgs);
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
				return methodProxy.invokeSuper(enhanced, args);
			}
			catch (Throwable e) {
				throw ExceptionUtil.unwrapThrowable(e);
			}
		}
	}

	private static class EnhancedDeserializationProxyImpl extends AbstractEnhancedDeserializationProxy implements MethodInterceptor {

		private EnhancedDeserializationProxyImpl(Class<?> type, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			super(type, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
		}

		public static Object createProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			Class<?> type = target.getClass();
			EnhancedDeserializationProxyImpl callback = new EnhancedDeserializationProxyImpl(type, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
			Object enhanced = createProxy(type, callback, objectFactory, constructorArgTypes, constructorArgs);
			PropertyCopier.copyBeanProperties(type, target, enhanced);
			return enhanced;
		}

		@Override
		public Object intercept(Object enhanced, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object o = super.invoke(enhanced, method, args);
			return o instanceof AbstractSerialStateHolder ? o : methodProxy.invokeSuper(o, args);
		}

		@Override
		protected AbstractSerialStateHolder newSerialStateHolder(Object userBean, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			return new CglibSerialStateHolder(userBean, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
		}
	}

	private static class LogHolder {
		private static final Log log = LogFactory.getLog(CglibProxyFactory.class);
	}
}
