package com.mawen.learn.mybatis.logging;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class LogFactory {

	public static final String MARKER = "MYBATIS";

	private static Constructor<? extends Log> logConstructor;

	static {

	}

	private LogFactory() {}

	public static Log getLog(Class<?> clazz) {
		return getLog(clazz.getName());
	}

	public static Log getLog(String logger) {
		try {
			return logConstructor.newInstance(logger);
		}
		catch (Throwable e) {
			throw new LogException("Error creating logger for logger: " + logger + ". Cause: ", e);
		}
	}
}
