package com.mawen.learn.mybatis.plugin;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class Plugin {

	private final Object target;
	private final Interceptor interceptor;
	private final Map<Class<?>, Set<Method>> signatureMap;

	public Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
		this.target = target;
		this.interceptor = interceptor;
		this.signatureMap = signatureMap;
	}


	public static Object wrap(Object target, Interceptor interceptor) {

	}

	private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
		interceptor.getClass().getAnnotation(Intercepts.class);
	}

	private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
		Set<Class<?>> interfaces = new HashSet<>();
		while (type != null) {
			for (Class<?> c : type.getInterfaces()) {
				if (signatureMap.containsKey(c)) {
					interfaces.add(c);
				}
			}
			type = type.getSuperclass();
		}
		return interfaces.toArray(new Class<?>[0]);
	}
}
