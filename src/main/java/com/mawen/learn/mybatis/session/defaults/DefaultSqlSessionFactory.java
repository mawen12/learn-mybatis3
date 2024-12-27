package com.mawen.learn.mybatis.session.defaults;

import java.sql.Connection;
import java.sql.SQLException;

import com.mawen.learn.mybatis.exceptions.ExceptionFactory;
import com.mawen.learn.mybatis.executor.ErrorContext;
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.mapping.Environment;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ExecutorType;
import com.mawen.learn.mybatis.session.SqlSession;
import com.mawen.learn.mybatis.session.SqlSessionFactory;
import com.mawen.learn.mybatis.session.TransactionIsolationLevel;
import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.transaction.TransactionFactory;
import com.mawen.learn.mybatis.transaction.managed.ManagedTransactionFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

	private final Configuration configuration;

	public DefaultSqlSessionFactory(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public SqlSession openSession() {
		return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
	}

	@Override
	public SqlSession openSession(boolean autoCommit) {
		return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, autoCommit);
	}

	@Override
	public SqlSession openSession(Connection connection) {
		return openSessionFromConnection(configuration.getDefaultExecutorType(), connection);
	}

	@Override
	public SqlSession openSession(TransactionIsolationLevel level) {
		return openSessionFromDataSource(configuration.getDefaultExecutorType(), level, false);
	}

	@Override
	public SqlSession openSession(ExecutorType execType) {
		return openSessionFromDataSource(execType, null, false);
	}

	@Override
	public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
		return openSessionFromDataSource(execType, null, autoCommit);
	}

	@Override
	public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
		return openSessionFromDataSource(execType, level, false);
	}

	@Override
	public SqlSession openSession(ExecutorType execType, Connection connection) {
		return openSessionFromConnection(execType, connection);
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
		Transaction tx = null;
		try {
			final Environment environment = configuration.getEnvironment();
			TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
			tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
			Executor executor = configuration.newExecutor(tx, execType);
			return new DefaultSqlSession(configuration, executor, autoCommit);
		}
		catch (Exception e) {
			closeTransaction(tx);
			throw ExceptionFactory.wrapException("Error opening session. Cause: " + e, e);
		}
		finally {
			ErrorContext.instance().reset();
		}
	}

	private SqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
		try {
			boolean autoCommit;
			try {
				autoCommit = connection.getAutoCommit();
			}
			catch (SQLException e) {
				autoCommit = true;
			}

			final Environment environment = configuration.getEnvironment();
			TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
			Transaction tx = transactionFactory.newTransaction(connection);
			Executor executor = configuration.newExecutor(tx, execType);
			return new DefaultSqlSession(configuration, executor, autoCommit);
		}
		catch (Exception e) {
			throw ExceptionFactory.wrapException("Error opening session. Cause: " + e, e);
		}
		finally {
			ErrorContext.instance().reset();
		}
	}

	private TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {
		if (environment == null || environment.getTransactionFactory() == null) {
			return new ManagedTransactionFactory();
		}
		return environment.getTransactionFactory();
	}

	private void closeTransaction(Transaction tx) {
		if (tx != null) {
			try {
				tx.close();
			}
			catch (SQLException ignored) {

			}
		}
	}
}
