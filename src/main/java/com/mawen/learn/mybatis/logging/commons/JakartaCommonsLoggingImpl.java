package com.mawen.learn.mybatis.logging.commons;

import com.mawen.learn.mybatis.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class JakartaCommonsLoggingImpl implements Log {

	private final org.apache.commons.logging.Log log;

	public JakartaCommonsLoggingImpl(String clazz) {
		this.log = LogFactory.getLog(clazz);
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
