package com.mawen.learn.mybatis.logging.stdout;

import com.mawen.learn.mybatis.logging.Log;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class StdOutImpl implements Log {

	public StdOutImpl(String clazz) {

	}

	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	public boolean isTraceEnabled() {
		return true;
	}

	@Override
	public void error(String s, Throwable e) {
		System.err.println(s);
		e.printStackTrace(System.err);
	}

	@Override
	public void error(String s) {
		System.err.println(s);
	}

	@Override
	public void debug(String s) {
		System.out.println(s);
	}

	@Override
	public void trace(String s) {
		System.out.printf(s);
	}

	@Override
	public void warn(String s) {
		System.out.println(s);
	}
}
