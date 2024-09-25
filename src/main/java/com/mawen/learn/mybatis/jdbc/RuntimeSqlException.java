package com.mawen.learn.mybatis.jdbc;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/25
 */
public class RuntimeSqlException extends RuntimeException{

	private static final long serialVersionUID = 7397522138883525817L;

	public RuntimeSqlException() {
		super();
	}

	public RuntimeSqlException(String message) {
		super(message);
	}

	public RuntimeSqlException(String message, Throwable cause) {
		super(message, cause);
	}

	public RuntimeSqlException(Throwable cause) {
		super(cause);
	}
}
