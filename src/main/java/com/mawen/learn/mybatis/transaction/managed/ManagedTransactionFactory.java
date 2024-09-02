package com.mawen.learn.mybatis.transaction.managed;

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
public class ManagedTransactionFactory implements TransactionFactory {

	private boolean closeConnection = true;

	@Override
	public void setProperties(Properties props) {
		if (props != null) {
			String closeConnectionProperty = props.getProperty("closeConnection");
			if (closeConnectionProperty != null) {
				this.closeConnection = Boolean.parseBoolean(closeConnectionProperty);
			}
		}
	}

	@Override
	public Transaction newTransaction(Connection conn) {
		return new ManagedTransaction(conn, closeConnection);
	}

	@Override
	public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
		return new ManagedTransaction(ds, level, closeConnection);
	}
}
