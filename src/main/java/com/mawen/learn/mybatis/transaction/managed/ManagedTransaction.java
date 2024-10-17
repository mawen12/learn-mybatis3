package com.mawen.learn.mybatis.transaction.managed;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import com.mawen.learn.mybatis.session.TransactionIsolationLevel;
import com.mawen.learn.mybatis.transaction.Transaction;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class ManagedTransaction implements Transaction {

	private static final Log log = LogFactory.getLog(ManagedTransaction.class);

	private Connection connection;
	private final boolean closeConnection;

	private DataSource dataSource;
	private TransactionIsolationLevel level;

	public ManagedTransaction(Connection connection, boolean closeConnection) {
		this.connection = connection;
		this.closeConnection = closeConnection;
	}

	public ManagedTransaction(DataSource ds, TransactionIsolationLevel level, boolean closeConnection) {
		this.dataSource = ds;
		this.level = level;
		this.closeConnection = closeConnection;
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (connection == null) {
			openConnection();
		}
		return connection;
	}

	@Override
	public void commit() throws SQLException {
		// Does nothing
	}

	@Override
	public void rollback() throws SQLException {
		// Does nothing
	}

	@Override
	public void close() throws SQLException {
		if (this.closeConnection && this.connection != null) {
			if (log.isDebugEnabled()) {
				log.debug("Closing JDBC Connection [" + this.connection + "]");
			}
			this.connection.close();
		}
	}

	@Override
	public Integer getTimeout() throws SQLException {
		return null;
	}

	protected void openConnection() throws SQLException {
		if (log.isDebugEnabled()) {
			log.debug("Opening JDBC Connection");
		}
		this.connection = this.dataSource.getConnection();
		if (level != null) {
			this.connection.setTransactionIsolation(level.getLevel());
		}
	}
}
