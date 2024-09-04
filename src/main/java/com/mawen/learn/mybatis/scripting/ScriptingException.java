package com.mawen.learn.mybatis.scripting;

import com.mawen.learn.mybatis.exceptions.PersistenceException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class ScriptingException extends PersistenceException {

	private static final long serialVersionUID = 3456427816268518382L;

	public ScriptingException() {
		super();
	}

	public ScriptingException(String message) {
		super(message);
	}

	public ScriptingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptingException(Throwable cause) {
		super(cause);
	}
}
