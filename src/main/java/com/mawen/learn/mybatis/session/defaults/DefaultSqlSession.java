package com.mawen.learn.mybatis.session.defaults;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.builder.BuilderException;
import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.exceptions.ExceptionFactory;
import com.mawen.learn.mybatis.exceptions.TooManyResultsException;
import com.mawen.learn.mybatis.executor.BatchResult;
import com.mawen.learn.mybatis.executor.ErrorContext;
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.result.DefaultMapResultHandler;
import com.mawen.learn.mybatis.executor.result.DefaultResultContext;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.reflection.ParamNameResolver;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.session.SqlSession;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class DefaultSqlSession implements SqlSession {

	private final Configuration configuration;
	private final Executor executor;
	private final boolean autoCommit;
	private boolean dirty;
	private List<Cursor<?>> cursorList;

	public DefaultSqlSession(Configuration configuration, Executor executor, boolean autoCommit) {
		this.configuration = configuration;
		this.executor = executor;
		this.autoCommit = autoCommit;
		this.dirty = false;
	}

	public DefaultSqlSession(Configuration configuration, Executor executor) {
		this(configuration, executor, false);
	}

	@Override
	public <T> T selectOne(String statement) {
		return this.selectOne(statement, null);
	}

	@Override
	public <T> T selectOne(String statement, Object parameter) {
		List<T> list = this.selectList(statement, parameter);
		if (list.size() == 1) {
			return list.get(0);
		}
		else if (list.size() > 1) {
			throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
		}
		else {
			return null;
		}
	}

	@Override
	public <E> List<E> selectList(String statement) {
		return this.selectList(statement, null);
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter) {
		return this.selectList(statement, parameter, RowBounds.DEFAULT);
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
		return this.selectList(statement, parameter, rowBounds, Executor.NO_RESULT_HANDLER);
	}

	private <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
		try {
			MappedStatement ms = configuration.getMappedStatement(statement);
			return executor.query(ms, parameter, rowBounds, handler);
		}
		catch (Exception e) {
			throw ExceptionFactory.wrapException("Error querying database. Cause: " + e, e);
		}
		finally {
			ErrorContext.instance().reset();
		}
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
		return this.selectMap(statement, null, mapKey, RowBounds.DEFAULT);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
		return this.selectMap(statement,parameter,mapKey, RowBounds.DEFAULT);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
		final List<? extends V> list = selectList(statement, parameter, rowBounds);
		final DefaultMapResultHandler<K, V> mapResultHandler = new DefaultMapResultHandler<>(mapKey, configuration.getObjectFactory(), configuration.getObjectWrapperFactory(), configuration.getReflectorFactory());
		final DefaultResultContext<V> context = new DefaultResultContext<>();
		for (V o : list) {
			context.nextResultObject(o);
			mapResultHandler.handleResult(context);
		}
		return mapResultHandler.getMappedResults();
	}

	@Override
	public <T> Cursor<T> selectCursor(String statement) {
		return this.selectCursor(statement, null);
	}

	@Override
	public <T> Cursor<T> selectCursor(String statement, Object parameter) {
		return this.selectCursor(statement, parameter, RowBounds.DEFAULT);
	}

	@Override
	public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
		try {
			MappedStatement ms = configuration.getMappedStatement(statement);
			Cursor<T> cursor = executor.queryCursor(ms, wrapCollection(parameter), rowBounds);
			registerCursor(cursor);
			return cursor;
		}
		catch (Exception e) {
			throw ExceptionFactory.wrapException("Error querying database. Cause: " + e, e);
		}
		finally {
			ErrorContext.instance().reset();
		}
	}

	@Override
	public void select(String statement, ResultHandler handler) {
		this.select(statement, null, handler);
	}

	@Override
	public void select(String statement, Object parameter, ResultHandler handler) {
		this.select(statement, parameter, RowBounds.DEFAULT, handler);
	}

	@Override
	public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
		this.selectList(statement, parameter, rowBounds, handler);
	}

	@Override
	public int insert(String statement) {
		return this.insert(statement, null);
	}

	@Override
	public int insert(String statement, Object parameter) {
		return update(statement, parameter);
	}

	@Override
	public int update(String statement) {
		return this.update(statement, null);
	}

	@Override
	public int update(String statement, Object parameter) {
		try {
			this.dirty = true;
			MappedStatement ms = configuration.getMappedStatement(statement);
			return executor.update(ms, parameter);
		}
		catch (Exception e) {
			throw ExceptionFactory.wrapException("Error updating database. Cause: " + e, e);
		}
		finally {
			ErrorContext.instance().reset();
		}
	}

	@Override
	public int delete(String statement) {
		return this.update(statement, null);
	}

	@Override
	public int delete(String statement, Object parameter) {
		return this.update(statement, parameter);
	}

	@Override
	public void commit() {
		this.commit(false);
	}

	@Override
	public void commit(boolean force) {
		try {
			executor.commit(isCommitOrRollbackRequired(force));
			this.dirty = false;
		}
		catch (Exception e) {
			throw ExceptionFactory.wrapException("Error committing database. Cause: " + e, e);
		}
		finally {
			ErrorContext.instance().reset();
		}
	}

	@Override
	public void rollback() {
		this.rollback(false);
	}

	@Override
	public void rollback(boolean force) {
		try {
			executor.rollback(isCommitOrRollbackRequired(force));
			this.dirty = false;
		}
		catch (Exception e) {
			throw ExceptionFactory.wrapException("Error rolling back database. Cause: " + e, e);
		}
		finally {
			ErrorContext.instance().reset();
		}
	}

	@Override
	public List<BatchResult> flushStatements() {
		try {
			return executor.flushStatements();
		}
		catch (Exception e) {
			throw ExceptionFactory.wrapException("Error flushing database. Cause: " + e, e);
		}
		finally {
			ErrorContext.instance().reset();
		}
	}

	@Override
	public void close() {
		try {
			executor.close(isCommitOrRollbackRequired(false));
			closeCursors();
			this.dirty = false;
		}
		finally {
			ErrorContext.instance().reset();
		}
	}

	@Override
	public void clearCache() {
		executor.clearLocalCache();
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public <T> T getMapper(Class<T> type) {
		return configuration.getMapper(type, this);
	}

	@Override
	public Connection getConnection() {
		try {
			return executor.getTransaction().getConnection();
		}
		catch (SQLException e) {
			throw ExceptionFactory.wrapException("Error getting a new connection. Cause: " + e, e);
		}
	}

	private void closeCursors() {
		if (cursorList != null && !cursorList.isEmpty()) {
			for (Cursor<?> cursor : cursorList) {
				try {
					cursor.close();
				}
				catch (IOException e) {
					throw ExceptionFactory.wrapException("Error closing cursor. Cause: " + e, e);
				}
			}
			cursorList.clear();
		}
	}

	private <T> void registerCursor(Cursor<T> cursor) {
		if (cursorList == null) {
			cursorList = new ArrayList<>();
		}
		cursorList.add(cursor);
	}

	private boolean isCommitOrRollbackRequired(boolean force) {
		return (!autoCommit && dirty) || force;
	}

	private Object wrapCollection(Object object) {
		return ParamNameResolver.wrapToMapIfCollection(object, null);
	}

	public static class StrictMap<V> extends HashMap<String, V> {

		private static final long serialVersionUID = 6950506778160063893L;

		@Override
		public V get(Object key) {
			if (!super.containsKey(key)) {
				throw new BuilderException("Parameter '" + key + "' not found. Available parameters are " + this.keySet());
			}
			return super.get(key);
		}
	}
}
