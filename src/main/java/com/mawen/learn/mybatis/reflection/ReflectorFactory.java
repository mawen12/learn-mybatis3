package com.mawen.learn.mybatis.reflection;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public interface ReflectorFactory {

	boolean isClassCacheEnabled();

	void setClassCacheEnabled(boolean classCacheEnabled);

	Reflector findForClass(Class<?> type);
}
