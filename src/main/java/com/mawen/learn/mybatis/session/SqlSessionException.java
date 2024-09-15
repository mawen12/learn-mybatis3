package com.mawen.learn.mybatis.session;

import com.mawen.learn.mybatis.exceptions.PersistenceException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public class SqlSessionException extends PersistenceException {

	private static final long serialVersionUID = -2438021261631163213L;

	public SqlSessionException() {
		super();
	}

	public SqlSessionException(String message) {
		super(message);
	}

	public SqlSessionException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlSessionException(Throwable cause) {
		super(cause);
	}
}
