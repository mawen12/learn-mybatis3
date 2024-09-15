package com.mawen.learn.mybatis.executor.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.parameter.ParameterHandler;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.session.ResultHandler;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/12
 */
public interface StatementHandler {

	Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException;

	void parameterize(Statement statement) throws SQLException;

	void batch(Statement statement) throws SQLException;

	int update(Statement statement) throws SQLException;

	<E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;

	<E> Cursor<E> queryCursor(Statement statement) throws SQLException;

	BoundSql getBoundSql();

	ParameterHandler getParameterHandler();

}
