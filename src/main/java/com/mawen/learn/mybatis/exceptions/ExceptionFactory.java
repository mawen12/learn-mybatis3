package com.mawen.learn.mybatis.exceptions;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class ExceptionFactory {

	private ExceptionFactory() {}

	public static RuntimeException wrapException(String message, Exception e) {
		return new PersistenceException(ErrorConext.instance().message(message).cause(e).toString(), e);
	}
}
