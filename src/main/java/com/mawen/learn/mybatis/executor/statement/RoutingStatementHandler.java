package com.mawen.learn.mybatis.executor.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.executor.parameter.ParameterHandler;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/19
 */
public class RoutingStatementHandler implements StatementHandler{

	private final StatementHandler delegate;

	public RoutingStatementHandler(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {

		switch (ms.getStatementType()) {
			case STATEMENT:
				delegate = new SimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
				break;
			case PREPARED:
				delegate = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
				break;
			case CALLABLE:
				delegate = new CallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
				break;
			default:
				throw new ExecutorException("Unknown statement type: " + ms.getStatementType());
		}


	}

	@Override
	public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
		return delegate.prepare(connection,transactionTimeout);
	}

	@Override
	public void parameterize(Statement statement) throws SQLException {
		delegate.parameterize(statement);
	}

	@Override
	public void batch(Statement statement) throws SQLException {
		delegate.batch(statement);
	}

	@Override
	public int update(Statement statement) throws SQLException {
		return delegate.update(statement);
	}

	@Override
	public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
		return delegate.query(statement,resultHandler);
	}

	@Override
	public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
		return delegate.queryCursor(statement);
	}

	@Override
	public BoundSql getBoundSql() {
		return delegate.getBoundSql();
	}

	@Override
	public ParameterHandler getParameterHandler() {
		return delegate.getParameterHandler();
	}
}
