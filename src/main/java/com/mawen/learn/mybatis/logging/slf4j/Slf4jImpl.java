package com.mawen.learn.mybatis.logging.slf4j;


import com.mawen.learn.mybatis.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class Slf4jImpl implements Log {

	private Log log;

	public Slf4jImpl(String clazz) {
		Logger logger = LoggerFactory.getLogger(clazz);

		if (logger instanceof LocationAwareLogger) {
			try {
				log.getClass().getMethod("log", Marker.class, String.class, int.class, String.class, Object[].class, Throwable.class);
				log = new Slf4jLocationAwareLoggerImpl((LocationAwareLogger) logger);
				return;
			}
			catch (SecurityException | NoSuchMethodException ignored) {

			}
		}

		log = new Slf4jLoggerImpl(logger);
	}

	@Override
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	@Override
	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	@Override
	public void error(String s, Throwable e) {
		log.error(s, e);
	}

	@Override
	public void error(String s) {
		log.error(s);
	}

	@Override
	public void debug(String s) {
		log.debug(s);
	}

	@Override
	public void trace(String s) {
		log.trace(s);
	}

	@Override
	public void warn(String s) {
		log.warn(s);
	}
}
