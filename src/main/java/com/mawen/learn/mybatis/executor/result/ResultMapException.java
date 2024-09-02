package com.mawen.learn.mybatis.executor.result;

import com.mawen.learn.mybatis.exceptions.PersistenceException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public class ResultMapException extends PersistenceException {

	public ResultMapException() {
		super();
	}

	public ResultMapException(String message) {
		super(message);
	}

	public ResultMapException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResultMapException(Throwable cause) {
		super(cause);
	}
}
