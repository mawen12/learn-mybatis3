package com.mawen.learn.mybatis.session;

import java.sql.Connection;

/**
 * 事务隔离级别
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public enum TransactionIsolationLevel {
	/**
	 * 无隔离级别
	 */
	NONE(Connection.TRANSACTION_NONE),
	/**
	 * 读未提交隔离级别
	 */
	READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
	/**
	 * 读已提交隔离级别
	 */
	READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
	/**
	 * 可重复读隔离级别
	 */
	REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
	/**
	 * 可序列化隔离级别
	 */
	SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

	private final int level;

	TransactionIsolationLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
}
