package com.mawen.learn.mybatis.transaction.jdbc;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.session.TransactionIsolationLevel;
import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.transaction.TransactionFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class JdbcTransactionFactory implements TransactionFactory {

	private boolean skipSetAutoCommitOnClose;

	@Override
	public void setProperties(Properties props) {
		if (props == null) {
			return;
		}

		String value = props.getProperty("skipSetAutoCommitOnClose");
		if (value != null) {
			skipSetAutoCommitOnClose = Boolean.parseBoolean(value);
		}
	}

	@Override
	public Transaction newTransaction(Connection conn) {
		return new JdbcTransaction(conn);
	}

	@Override
	public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
		return new JdbcTransaction(ds, level, autoCommit, skipSetAutoCommitOnClose);
	}
}
