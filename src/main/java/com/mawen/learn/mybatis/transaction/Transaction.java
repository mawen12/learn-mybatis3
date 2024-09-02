package com.mawen.learn.mybatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public interface Transaction {

	Connection getConnection() throws SQLException;

	void commit() throws SQLException;

	void rollback() throws SQLException;

	void close() throws SQLException;

	Integer getTimeout() throws SQLException;
}
