package com.mawen.learn.mybatis.plugin;

import com.mawen.learn.mybatis.exceptions.PersistenceException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class PluginException extends PersistenceException {

	private static final long serialVersionUID = 3602068821001108249L;

	public PluginException() {
		super();
	}

	public PluginException(String message) {
		super(message);
	}

	public PluginException(String message, Throwable cause) {
		super(message, cause);
	}

	public PluginException(Throwable cause) {
		super(cause);
	}
}
