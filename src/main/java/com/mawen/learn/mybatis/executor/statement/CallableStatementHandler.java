package com.mawen.learn.mybatis.executor.statement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.executor.keygen.KeyGenerator;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.ParameterMapping;
import com.mawen.learn.mybatis.mapping.ParameterMode;
import com.mawen.learn.mybatis.mapping.ResultSetType;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.type.JdbcType;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/19
 */
public class CallableStatementHandler extends BaseStatementHandler{

	public CallableStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
		super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
	}

	@Override
	protected Statement instantiateStatement(Connection connection) throws SQLException {
		String sql = boundSql.getSql();
		if (mappedStatement.getResultSetType() == ResultSetType.DEFAULT) {
			return connection.prepareCall(sql);
		}
		else {
			return connection.prepareCall(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
		}
	}

	@Override
	public void parameterize(Statement statement) throws SQLException {
		CallableStatement cs = (CallableStatement) statement;
		registerOutputParameters(cs);
		parameterHandler.setParameters(cs);
	}

	@Override
	public void batch(Statement statement) throws SQLException {
		CallableStatement cs = (CallableStatement) statement;
		cs.addBatch();
	}

	@Override
	public int update(Statement statement) throws SQLException {
		CallableStatement cs = (CallableStatement) statement;
		cs.execute();

		int rows = cs.getUpdateCount();
		Object parameterObject = boundSql.getParameterObject();
		KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
		keyGenerator.processAfter(executor, mappedStatement, cs, parameterObject);
		resultSetHandler.handleOutputParameters(cs);
		return rows;
	}

	@Override
	public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
		CallableStatement cs = (CallableStatement) statement;
		cs.execute();
		List<E> resultList = resultSetHandler.handleResultSets(cs);
		resultSetHandler.handleOutputParameters(cs);
		return resultList;
	}

	@Override
	public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
		CallableStatement cs = (CallableStatement) statement;
		cs.execute();
		Cursor<E> resultList = resultSetHandler.handleCursorResultSets(cs);
		resultSetHandler.handleOutputParameters(cs);
		return resultList;
	}

	private void registerOutputParameters(CallableStatement cs) throws SQLException {
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		for (int i = 0; i < parameterMappings.size(); i++) {
			ParameterMapping parameterMapping = parameterMappings.get(i);
			if (parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT) {
				if (null == parameterMapping.getJdbcType()) {
					throw new ExecutorException("The JDBC Type must be specified for output parameter. Parameter: " + parameterMapping.getProperty());
				}
				else {
					if (parameterMapping.getNumericScale() != null && (parameterMapping.getJdbcType() == JdbcType.NUMERIC || parameterMapping.getJdbcType() == JdbcType.DECIMAL)) {
						cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE, parameterMapping.getNumericScale());
					}
					else {
						if (parameterMapping.getJdbcTypeName() == null) {
							cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE);
						}
						else {
							cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE, parameterMapping.getJdbcTypeName());
						}
					}
				}
			}
		}
	}
}
