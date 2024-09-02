package com.mawen.learn.mybatis.session;

import java.sql.Connection;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public interface SqlSessionFactory {

	SqlSession openSession();

	SqlSession openSession(boolean autoCommit);

	SqlSession openSession(Connection connection);

	SqlSession openSession(TransactionIsolationLevel level);

	SqlSession openSession(ExecutorType execType);

	SqlSession openSession(ExecutorType execType, boolean autoCommit);

	SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);

	SqlSession openSession(ExecutorType execType, Connection connection);

	Configuration getConfiguration();
}
