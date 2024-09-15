package com.mawen.learn.mybatis.executor.resultset;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.mawen.learn.mybatis.cursor.Cursor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/13
 */
public interface ResultSetHandler {

	<E> List<E> handleResultSets(Statement statement) throws SQLException;

	<E> Cursor<E> handleCursorResultSets(Statement statement) throws SQLException;

	void handleOutputParameters(CallableStatement cs) throws SQLException;
}
