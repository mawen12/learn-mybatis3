package com.mawen.learn.mybatis.executor;

import com.mawen.learn.mybatis.exceptions.PersistenceException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class ExecutorException extends PersistenceException {

	private static final long serialVersionUID = 1258875392666094474L;

	public ExecutorException() {
		super();
	}

	public ExecutorException(String message) {
		super(message);
	}

	public ExecutorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExecutorException(Throwable cause) {
		super(cause);
	}
}
