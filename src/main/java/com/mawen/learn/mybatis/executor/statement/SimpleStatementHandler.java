package com.mawen.learn.mybatis.executor.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.mawen.learn.mybatis.executor.keygen.KeyGenerator;
import com.mawen.learn.mybatis.executor.keygen.SelectKeyGenerator;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/13
 */
public class SimpleStatementHandler extends BaseStatementHandler {

	protected SimpleStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
		super(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
	}

	@Override
	protected Statement instantiateStatement(Connection connection) throws SQLException {
		return null;
	}

	@Override
	public void parameterize(Statement statement) throws SQLException {
		// N/A
	}

	@Override
	public void batch(Statement statement) throws SQLException {
		String sql = boundSql.getSql();
		statement.addBatch(sql);
	}

	@Override
	public int update(Statement statement) throws SQLException {
		String sql = boundSql.getSql();
		Object parameterObject = boundSql.getParameterObject();
		KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();

		int rows;
		if (keyGenerator instanceof Jdbc3KeyGenerator) {
			statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
			rows = statement.getUpdateCount();
			keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
		}
		else if (keyGenerator instanceof SelectKeyGenerator) {
			statement.execute(sql);
			rows = statement.getUpdateCount();
			keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
		}
		else {
			statement.execute(sql);
			rows = statement.getUpdateCount();
		}
		return rows;
	}

	@Override
	public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
		String sql = boundSql.getSql();
		statement.execute(sql);
		return resultSetHandler.handleResultSets(statement);
	}

	@Override
	public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
		return null;
	}
}
