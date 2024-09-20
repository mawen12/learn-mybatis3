package com.mawen.learn.mybatis.logging.nologging;

import com.mawen.learn.mybatis.logging.Log;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class NoLoggingImpl implements Log {

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public void error(String s, Throwable e) {

	}

	@Override
	public void error(String s) {

	}

	@Override
	public void debug(String s) {

	}

	@Override
	public void trace(String s) {

	}

	@Override
	public void warn(String s) {

	}
}
