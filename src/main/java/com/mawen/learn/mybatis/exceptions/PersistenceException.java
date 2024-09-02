package com.mawen.learn.mybatis.exceptions;

/**
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class PersistenceException extends RuntimeException {

	private static final long serialVersionUID = 410875315871556400L;

	public PersistenceException() {
		super();
	}

	public PersistenceException(String message) {
		super(message);
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public PersistenceException(Throwable cause) {
		super(cause);
	}
}
