package com.mawen.learn.mybatis.transaction.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import com.mawen.learn.mybatis.session.TransactionIsolationLevel;
import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.transaction.TransactionException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class JdbcTransaction implements Transaction {

	private static final Log log = LogFactory.getLog(JdbcTransaction.class);

	protected Connection connection;

	protected DataSource dataSource;
	protected TransactionIsolationLevel level;
	protected boolean autoCommit;
	protected boolean skipSetAutoCommitOnClose;

	public JdbcTransaction(Connection connection) {
		this.connection = connection;
	}

	public JdbcTransaction(DataSource ds, TransactionIsolationLevel desiredLevel, boolean desiredAutoCommit) {
		this(ds, desiredLevel, desiredAutoCommit, false);
	}

	public JdbcTransaction(DataSource ds, TransactionIsolationLevel desiredLevel, boolean desiredAutoCommit, boolean skipSetAutoCommitOnClose) {
		this.dataSource = ds;
		this.level = desiredLevel;
		this.autoCommit = desiredAutoCommit;
		this.skipSetAutoCommitOnClose = skipSetAutoCommitOnClose;
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
		if (connection != null && !connection.getAutoCommit()) {
			if (log.isDebugEnabled()) {
				log.debug("Committing JDBC Connection [" + connection + "]");
			}
			connection.commit();
		}
	}

	@Override
	public void rollback() throws SQLException {
		if (connection != null && !connection.getAutoCommit()) {
			if (log.isDebugEnabled()) {
				log.debug("Rolling back JDBC Connection [" + connection + "]");
			}
			connection.rollback();
		}
	}

	@Override
	public void close() throws SQLException {
		if (connection != null) {
			resetAutoCommit();
			if (log.isDebugEnabled()) {
				log.debug("Closing JDBC Connection [" + connection + "]");
			}
			connection.close();
		}
	}

	@Override
	public Integer getTimeout() throws SQLException {
		return null;
	}

	protected void setDesiredAutoCommit(boolean desiredAutoCommit) {
		try {
			if (connection.getAutoCommit() != desiredAutoCommit) {
				if (log.isDebugEnabled()) {
					log.debug("Setting commit to " + desiredAutoCommit + " on JDBC Connection [" + connection + "]");
				}
				connection.setAutoCommit(desiredAutoCommit);
			}
		}
		catch (Exception e) {
			throw new TransactionException("Error configuring AutoCommit. Your driver may not support getAutoCommit() or setAutoCommit()." +
			                               "Required setting: " + desiredAutoCommit + ". Cause: " + e, e);
		}
	}

	protected void resetAutoCommit() {
		try {
			if (!skipSetAutoCommitOnClose && !connection.getAutoCommit()) {
				if (log.isDebugEnabled()) {
					log.debug("Resetting autocommit to true on JDBC Connection [" + connection + "]");
				}
				connection.setAutoCommit(true);
			}
		}
		catch (SQLException e) {
			if (log.isDebugEnabled()) {
				log.debug("Error resetting autocommit to true before closing JDBC Connection. Cause: " + e);
			}
		}
	}

	protected void openConnection() throws SQLException {
		if (log.isDebugEnabled()) {
			log.debug("Open JDBC Connection");
		}
		connection = dataSource.getConnection();
		if (level != null) {
			connection.setTransactionIsolation(level.getLevel());
		}
		setDesiredAutoCommit(autoCommit);
	}
}
