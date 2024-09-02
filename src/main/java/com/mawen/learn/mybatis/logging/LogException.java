package com.mawen.learn.mybatis.logging;

import com.mawen.learn.mybatis.exceptions.PersistenceException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class LogException extends PersistenceException {

	private static final long serialVersionUID = 7466657391275586841L;

	public LogException() {
		super();
	}

	public LogException(String message) {
		super(message);
	}

	public LogException(String message, Throwable cause) {
		super(message, cause);
	}

	public LogException(Throwable cause) {
		super(cause);
	}
}
