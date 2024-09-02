package com.mawen.learn.mybatis.exceptions;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class TooManyResultsException extends PersistenceException{

	private static final long serialVersionUID = 4829845274134314116L;

	public TooManyResultsException() {
		super();
	}

	public TooManyResultsException(String message) {
		super(message);
	}

	public TooManyResultsException(String message, Throwable cause) {
		super(message, cause);
	}

	public TooManyResultsException(Throwable cause) {
		super(cause);
	}
}
