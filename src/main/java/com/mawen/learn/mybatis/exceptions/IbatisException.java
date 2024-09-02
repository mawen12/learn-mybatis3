package com.mawen.learn.mybatis.exceptions;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class IbatisException extends RuntimeException{

	private static final long serialVersionUID = 7033969490913127344L;

	public IbatisException() {
		super();
	}

	public IbatisException(String message) {
		super(message);
	}

	public IbatisException(String message, Throwable cause) {
		super(message, cause);
	}

	public IbatisException(Throwable cause) {
		super(cause);
	}
}
