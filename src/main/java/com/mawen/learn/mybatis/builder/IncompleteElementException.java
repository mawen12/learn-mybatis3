package com.mawen.learn.mybatis.builder;

import com.mawen.learn.mybatis.exceptions.PersistenceException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class IncompleteElementException extends PersistenceException {

	public IncompleteElementException() {
		super();
	}

	public IncompleteElementException(String message) {
		super(message);
	}

	public IncompleteElementException(String message, Throwable cause) {
		super(message, cause);
	}

	public IncompleteElementException(Throwable cause) {
		super(cause);
	}
}
