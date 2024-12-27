package com.mawen.learn.mybatis.session;

import java.sql.Connection;

/**
 * 负责创建SqlSession的工厂。
 * 简单工厂设计模式。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public interface SqlSessionFactory {

	/**
	 * 创建会话
	 */
	SqlSession openSession();

	/**
	 * 创建自动提交的会话
	 */
	SqlSession openSession(boolean autoCommit);

	/**
	 * 使用现有数据库连接创建会话
	 */
	SqlSession openSession(Connection connection);

	/**
	 * 创建
	 */
	SqlSession openSession(TransactionIsolationLevel level);

	SqlSession openSession(ExecutorType execType);

	SqlSession openSession(ExecutorType execType, boolean autoCommit);

	SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);

	SqlSession openSession(ExecutorType execType, Connection connection);

	Configuration getConfiguration();
}
