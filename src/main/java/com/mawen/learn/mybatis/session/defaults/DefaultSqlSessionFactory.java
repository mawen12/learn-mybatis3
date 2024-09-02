package com.mawen.learn.mybatis.session.defaults;

import java.sql.Connection;

import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ExecutorType;
import com.mawen.learn.mybatis.session.SqlSession;
import com.mawen.learn.mybatis.session.SqlSessionFactory;
import com.mawen.learn.mybatis.session.TransactionIsolationLevel;
import com.mawen.learn.mybatis.transaction.Transaction;

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
		return null;
	}

	@Override
	public SqlSession openSession(boolean autoCommit) {
		return null;
	}

	@Override
	public SqlSession openSession(Connection connection) {
		return null;
	}

	@Override
	public SqlSession openSession(TransactionIsolationLevel level) {
		return null;
	}

	@Override
	public SqlSession openSession(ExecutorType execType) {
		return null;
	}

	@Override
	public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
		return null;
	}

	@Override
	public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
		return null;
	}

	@Override
	public SqlSession openSession(ExecutorType execType, Connection connection) {
		return null;
	}

	@Override
	public Configuration getConfiguration() {
		return null;
	}

	private SqlSession openSqlSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
		Transaction tx = null;
		try {
			configuration.getEnvironment();
		}
	}
}
