package com.mawen.learn.mybatis.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.statement.StatementHandler;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.transaction.Transaction;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public class ReuseExecutor extends BaseExecutor {

	private final Map<String, Statement> statementMap = new HashMap<>();

	public ReuseExecutor(Configuration configuration, Transaction transaction) {
		super(configuration, transaction);
	}

	@Override
	protected int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
		Configuration configuration = ms.getConfiguration();
		StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
		Statement stmt = prepareStatement(handler, ms.getStatementLog());
		return handler.update(stmt);
	}

	@Override
	protected List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
		for (Statement stmt : statementMap.values()) {
			closeStatement(stmt);
		}
		statementMap.clear();
		return Collections.emptyList();
	}

	@Override
	protected <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
		Configuration configuration = ms.getConfiguration();
		StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
		Statement stmt = prepareStatement(handler, ms.getStatementLog());
		return handler.query(stmt,resultHandler);
	}

	@Override
	protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
		Configuration configuration = ms.getConfiguration();
		StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
		Statement stmt = prepareStatement(handler, ms.getStatementLog());
		return handler.queryCursor(stmt);
	}

	private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
		Statement stmt;
		BoundSql boundSql = handler.getBoundSql();
		String sql = boundSql.getSql();
		if (hasStatementFor(sql)) {
			stmt = getStatement(sql);
			applyTransactionTimeout(stmt);
		}
		else {
			Connection connection = getConnection(statementLog);
			stmt = handler.prepare(connection, transaction.getTimeout());
			putStatement(sql, stmt);
		}
		handler.parameterize(stmt);
		return stmt;
	}

	private boolean hasStatementFor(String sql) {
		try {
			Statement statement = statementMap.get(sql);
			return statement != null && !statement.getConnection().isClosed();
		}
		catch (SQLException e) {
			return false;
		}
	}

	private Statement getStatement(String sql) {
		return statementMap.get(sql);
	}

	private void putStatement(String sql, Statement statement) {
		statementMap.put(sql, statement);
	}
}
