package com.mawen.learn.mybatis.logging;

import java.lang.reflect.Constructor;

import com.mawen.learn.mybatis.logging.commons.JakartaCommonsLoggingImpl;
import com.mawen.learn.mybatis.logging.jdk14.Jdk14LoggingImpl;
import com.mawen.learn.mybatis.logging.log4j2.Log4j2Impl;
import com.mawen.learn.mybatis.logging.nologging.NoLoggingImpl;
import com.mawen.learn.mybatis.logging.slf4j.Slf4jImpl;
import com.mawen.learn.mybatis.logging.stdout.StdOutImpl;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class LogFactory {

	public static final String MARKER = "MYBATIS";

	private static Constructor<? extends Log> logConstructor;

	static {
		tryImplementation(LogFactory::useSlf4jLogging);
		tryImplementation(LogFactory::useCommonsLogging);
		tryImplementation(LogFactory::useLog4j2Logging);
		tryImplementation(LogFactory::useJdkLogging);
		tryImplementation(LogFactory::useNoLogging);
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

	public static synchronized void useCustomLogging(Class<? extends Log> clazz) {
		setImplementation(clazz);
	}

	public static synchronized void useSlf4jLogging() {
		setImplementation(Slf4jImpl.class);
	}

	public static synchronized void useCommonsLogging() {
		setImplementation(JakartaCommonsLoggingImpl.class);
	}

	public static synchronized void useLog4j2Logging() {
		setImplementation(Log4j2Impl.class);
	}

	public static synchronized void useJdkLogging() {
		setImplementation(Jdk14LoggingImpl.class);
	}

	public static synchronized void useStdOutLogging() {
		setImplementation(StdOutImpl.class);
	}

	public static synchronized void useNoLogging() {
		setImplementation(NoLoggingImpl.class);
	}

	private static void tryImplementation(Runnable runnable) {
		if (logConstructor == null) {
			try {
				runnable.run();
			}
			catch (Throwable t) {

			}
		}
	}

	private static void setImplementation(Class<? extends Log> implClass) {
		try {
			Constructor<? extends Log> candidate = implClass.getConstructor(String.class);
			Log log = candidate.newInstance(LogFactory.class.getName());
			if (log.isDebugEnabled()) {
				log.debug("Logging initialized using '" + implClass + "' adapter.");
			}
			logConstructor = candidate;
		}
		catch (Throwable t) {
			throw new LogException("Error setting Log Implementation. Cause: " + t, t);
		}
	}
}
