package com.mawen.learn.mybatis.executor.loader.javassist;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mawen.learn.mybatis.executor.loader.AbstractEnhancedDeserializationProxy;
import com.mawen.learn.mybatis.executor.loader.AbstractSerialStateHolder;
import com.mawen.learn.mybatis.executor.loader.ProxyFactory;
import com.mawen.learn.mybatis.executor.loader.ResultLoaderMap;
import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.property.PropertyCopier;
import com.mawen.learn.mybatis.session.Configuration;
import javassist.util.proxy.MethodHandler;

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
		return null;
	}

	private static class EnhancedResultObjectProxyImpl implements MethodHandler {
		private final Class<?> type;
		private final ResultLoaderMap resultLoaderMap;
		private final boolean aggressive;
		private final Set<String> lazyLoadTriggerMethods;
		private final ObjectFactory objectFactory;
		private final List<Class<?>> constructorArgTypes;
		private final List<Object> constructorArgs;

		private EnhancedResultObjectProxyImpl(Class<?> type, ResultLoaderMap resultLoaderMap, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
			this.type = type;
			this.resultLoaderMap = resultLoaderMap;
			this.aggressive = configuration.isAggressiveLazyLoading();
			this.lazyLoadTriggerMethods = configuration.getLazyLoadTriggerMethods();
			this.objectFactory = objectFactory;
			this.constructorArgTypes = constructorArgTypes;
			this.constructorArgs = constructorArgs;
		}

		public static Object createProxy() {

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
			Object enhanced = createProxy(type, callback, constructorArgTypes, constructorArgs);
			PropertyCopier.copyBeanProperties(type,target,enhanced);
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
