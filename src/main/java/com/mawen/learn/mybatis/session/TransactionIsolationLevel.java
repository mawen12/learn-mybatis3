package com.mawen.learn.mybatis.session;

import java.sql.Connection;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public enum TransactionIsolationLevel {
	NONE(Connection.TRANSACTION_NONE),
	READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
	READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
	REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
	SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

	private final int level;

	TransactionIsolationLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
}
