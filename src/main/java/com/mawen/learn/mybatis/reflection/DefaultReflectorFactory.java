package com.mawen.learn.mybatis.reflection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mawen.learn.mybatis.util.MapUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class DefaultReflectorFactory implements ReflectorFactory {

	private boolean classCacheEnabled = true;
	private final ConcurrentMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<>();

	@Override
	public boolean isClassCacheEnabled() {
		return classCacheEnabled;
	}

	@Override
	public void setClassCacheEnabled(boolean classCacheEnabled) {
		this.classCacheEnabled = classCacheEnabled;
	}

	@Override
	public Reflector findForClass(Class<?> type) {
		if (classCacheEnabled) {
			return MapUtil.computIfAbsent(reflectorMap, type, Reflector::new);
		}
		else {
			return new Reflector(type);
		}
	}
}
