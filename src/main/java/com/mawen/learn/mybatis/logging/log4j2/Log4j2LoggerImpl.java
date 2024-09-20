package com.mawen.learn.mybatis.logging.log4j2;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class Log4j2LoggerImpl implements Log {

	private static final Marker MARKER = MarkerManager.getMarker(LogFactory.MARKER);

	private final Logger log;

	public Log4j2LoggerImpl(Logger logger) {
		this.log = logger;
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
		log.error(MARKER, s, e);
	}

	@Override
	public void error(String s) {
		log.error(MARKER, s);
	}

	@Override
	public void debug(String s) {
		log.debug(MARKER, s);
	}

	@Override
	public void trace(String s) {
		log.trace(MARKER, s);
	}

	@Override
	public void warn(String s) {
		log.warn(MARKER, s);
	}
}
