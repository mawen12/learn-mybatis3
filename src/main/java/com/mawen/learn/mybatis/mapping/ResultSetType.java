package com.mawen.learn.mybatis.mapping;

import java.sql.ResultSet;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public enum ResultSetType {
	DEFAULT(-1),
	FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),
	SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),
	SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE);


	private final int value;

	ResultSetType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
