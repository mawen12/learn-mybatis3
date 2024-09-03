package com.mawen.learn.mybatis.reflection;

import com.mawen.learn.mybatis.exceptions.PersistenceException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class ReflectionException extends PersistenceException {

	private static final long serialVersionUID = 2193273670839711756L;

	public ReflectionException() {
		super();
	}

	public ReflectionException(String message) {
		super(message);
	}

	public ReflectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReflectionException(Throwable cause) {
		super(cause);
	}
}
