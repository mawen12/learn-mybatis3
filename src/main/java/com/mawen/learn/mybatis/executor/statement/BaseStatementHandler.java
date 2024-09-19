package com.mawen.learn.mybatis.executor.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.mawen.learn.mybatis.executor.ErrorContext;
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.executor.keygen.KeyGenerator;
import com.mawen.learn.mybatis.executor.parameter.ParameterHandler;
import com.mawen.learn.mybatis.executor.resultset.ResultSetHandler;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/12
 */
public abstract class BaseStatementHandler implements StatementHandler {

	protected final Configuration configuration;
	protected final ObjectFactory objectFactory;
	protected final TypeHandlerRegistry typeHandlerRegistry;
	protected final ResultSetHandler resultSetHandler;
	protected final ParameterHandler parameterHandler;

	protected final Executor executor;
	protected final MappedStatement mappedStatement;
	protected final RowBounds rowBounds;

	protected BoundSql boundSql;

	protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
		this.configuration = mappedStatement.getConfiguration();
		this.executor = executor;
		this.mappedStatement = mappedStatement;
		this.rowBounds = rowBounds;

		this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
		this.objectFactory = configuration.getObjectFactory();

		if (boundSql == null) {
			generateKeys(parameterObject);
			boundSql = mappedStatement.getBoundSql(parameterObject);
		}

		this.boundSql = boundSql;

		this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
		this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
	}

	@Override
	public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
		ErrorContext.instance().sql(boundSql.getSql());
		Statement statement = null;
		try {
			statement = instantiateStatement(connection);
			setStatementTimeout(statement, transactionTimeout);
			setFetchSize(statement);
			return statement;
		}
		catch (SQLException e) {
			closeStatement(statement);
			throw e;
		}
		catch (Exception e) {
			closeStatement(statement);
			throw new ExecutorException("Error preparing statement. Cause: " + e, e);
		}
	}

	@Override
	public BoundSql getBoundSql() {
		return boundSql;
	}

	@Override
	public ParameterHandler getParameterHandler() {
		return parameterHandler;
	}

	protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

	protected void setStatementTimeout(Statement statement, Integer transactionTimeout) throws SQLException {
		Integer queryTimeout = null;
		if (mappedStatement.getTimeout() == null) {
			queryTimeout = mappedStatement.getTimeout();
		}
		else if (configuration.getDefaultStatementTimeout() != null) {
			queryTimeout = configuration.getDefaultStatementTimeout();
		}

		if (queryTimeout != null) {
			statement.setQueryTimeout(queryTimeout);
		}

		StatementUtil.applyTransactionTimeout(statement,queryTimeout,transactionTimeout);
	}

	protected void setFetchSize(Statement statement) throws SQLException {
		Integer fetchSize = mappedStatement.getFetchSize();
		if (fetchSize != null) {
			statement.setFetchSize(fetchSize);
			return;
		}

		Integer defaultFetchSize = configuration.getDefaultFetchSize();
		if (defaultFetchSize != null) {
			statement.setFetchSize(defaultFetchSize);
		}
	}

	protected void closeStatement(Statement statement) {
		try {
			if (statement != null) {
				statement.close();
			}
		}
		catch (SQLException e) {
			// ignored
		}
	}

	protected void generateKeys(Object parameter) {
		KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
		ErrorContext.instance().store();
		keyGenerator.processBefore(executor,mappedStatement,null,parameter);
		ErrorContext.instance().recall();
	}
}
