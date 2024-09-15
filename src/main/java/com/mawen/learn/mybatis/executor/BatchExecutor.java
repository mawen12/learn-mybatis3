package com.mawen.learn.mybatis.executor;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.mawen.learn.mybatis.executor.keygen.KeyGenerator;
import com.mawen.learn.mybatis.executor.keygen.NoKeyGenerator;
import com.mawen.learn.mybatis.executor.statement.StatementHandler;
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
public class BatchExecutor extends BaseExecutor {

	public static final int BATCH_UPDATE_RETURN_VALUE = Integer.MIN_VALUE + 1002;

	private final List<Statement> statementList = new ArrayList<>();
	private final List<BatchResult> batchResultList = new ArrayList<>();
	private String currentSql;
	private MappedStatement currentStatement;

	public BatchExecutor(Configuration configuration, Transaction transaction) {
		super(configuration, transaction);
	}

	@Override
	protected int doUpdate(MappedStatement ms, Object parameterObject) throws SQLException {
		final Configuration configuration = ms.getConfiguration();
		final StatementHandler handler = configuration.newStatementHandler(this, ms, parameterObject, RowBounds.DEFAULT, null, null);
		final BoundSql boundSql = handler.getBoundSql();
		final String sql = boundSql.getSql();
		final Statement stmt;

		if (sql.equals(currentSql) && ms.equals(currentStatement)) {
			int last = statementList.size() - 1;
			stmt = statementList.get(last);
			applyTransactionTimeout(stmt);
			handler.parameterize(stmt);
			BatchResult batchResult = batchResultList.get(last);
			batchResult.addParameterObject(parameterObject);
		}
		else {
			Connection connection = getConnection(ms.getStatementLog());
			stmt = handler.prepare(connection, transaction.getTimeout());
			handler.parameterize(stmt);
			currentSql = sql;
			currentStatement = ms;
			statementList.add(stmt);
			batchResultList.add(new BatchResult(ms, sql, parameterObject));
		}
		handler.batch(stmt);
		return BATCH_UPDATE_RETURN_VALUE;
	}

	@Override
	protected List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
		try {
			List<BatchResult> results = new ArrayList<>();
			if (isRollback) {
				return Collections.emptyList();
			}

			for (int i = 0, n = statementList.size(); i < n; i++) {
				Statement stmt = statementList.get(i);
				applyTransactionTimeout(stmt);
				BatchResult batchResult = batchResultList.get(i);
				try {
					batchResult.setUpdateCounts(stmt.executeBatch());
					MappedStatement ms = batchResult.getMappedStatement();
					List<Object> parameterObjects = batchResult.getParameterObjects();
					KeyGenerator keyGenerator = ms.getKeyGenerator();
					if (Jdbc3KeyGenerator.class.equals(keyGenerator.getClass())) {
						Jdbc3KeyGenerator jdbc3KeyGenerator = (Jdbc3KeyGenerator) keyGenerator;
						jdbc3KeyGenerator.processBatch(ms, stmt, parameterObjects);
					}
					else if (!NoKeyGenerator.class.equals(keyGenerator.getClass())) {
						for (Object parameter : parameterObjects) {
							keyGenerator.processAfter(this, ms, stmt, parameter);
						}
					}
					closeStatement(stmt);
				}
				catch (BatchUpdateException e) {
					StringBuilder message = new StringBuilder();
					message.append(batchResult.getMappedStatement().getId())
							.append(" (batch index #")
							.append(i + 1)
							.append(")")
							.append(" failed.");

					if (i > 0) {
						message.append(" ")
								.append(i)
								.append(" prior sub executor(s) completed successfully, but will be rolled back.");
					}
					throw new BatchExecutorException(message.toString(), e, results, batchResult);
				}
				results.add(batchResult);
			}
			return results;
		}
		finally {
			for (Statement statement : statementList) {
				closeStatement(statement);
			}
			currentSql = null;
			statementList.clear();
			batchResultList.clear();
		}
	}

	@Override
	protected <E> List<E> doQuery(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
		Statement stmt = null;
		try {
			flushStatements();
			Configuration configuration = ms.getConfiguration();
			StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameterObject, rowBounds, resultHandler, boundSql);
			Connection connection = getConnection(ms.getStatementLog());
			stmt = handler.prepare(connection,transaction.getTimeout());
			handler.parameterize(stmt);
			return handler.query(stmt, resultHandler);
		}
		finally {
			closeStatement(stmt);
		}
	}

	@Override
	protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
		flushStatements();
		Configuration configuration = ms.getConfiguration();
		StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
		Connection connection = getConnection(ms.getStatementLog());
		Statement stmt = handler.prepare(connection, transaction.getTimeout());
		handler.parameterize(stmt);
		Cursor<E> cursor = handler.queryCursor(stmt);
		stmt.closeOnCompletion();
		return cursor;
	}
}
