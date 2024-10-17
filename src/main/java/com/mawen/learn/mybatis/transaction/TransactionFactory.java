package com.mawen.learn.mybatis.transaction;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.session.TransactionIsolationLevel;

/**
 * 用于创建事务的工厂
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public interface TransactionFactory {

	default void setProperties(Properties props) {}

	Transaction newTransaction(Connection conn);

	Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit);
}
