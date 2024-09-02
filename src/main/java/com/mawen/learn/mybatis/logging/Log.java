package com.mawen.learn.mybatis.logging;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public interface Log {

	boolean isDebugEnabled();

	boolean isTraceEnabled();

	void error(String s, Throwable e);

	void error(String s);

	void debug(String s);

	void trace(String s);

	void warn(String s);

}
