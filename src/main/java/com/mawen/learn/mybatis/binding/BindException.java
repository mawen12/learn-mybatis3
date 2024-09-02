package com.mawen.learn.mybatis.binding;

import com.mawen.learn.mybatis.exceptions.PersistenceException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/1
 */
public class BindException extends PersistenceException {

	private static final long serialVersionUID = 1794340314758463210L;

	public BindException() {
		super();
	}

	public BindException(String message) {
		super(message);
	}

	public BindException(String message, Throwable cause) {
		super(message, cause);
	}

	public BindException(Throwable cause) {
		super(cause);
	}
}
