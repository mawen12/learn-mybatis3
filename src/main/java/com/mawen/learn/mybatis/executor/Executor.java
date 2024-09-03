package com.mawen.learn.mybatis.executor;

import java.sql.SQLException;
import java.util.List;

import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.transaction.Transaction;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public interface Executor {

	ResultHandler NO_RESULT_HANDLER = null;

	int update(MappedStatement ms, Object parameter) throws SQLException;

	<E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException;

	<E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

	<E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;

	List<BatchResult> flushStatements() throws SQLException;

	void commit(boolean required) throws SQLException;

	void rollback(boolean required) throws SQLException;

	CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

	boolean isCached(MappedStatement ms, CacheKey key);

	void clearLocalCache();

	void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

	Transaction getTransaction();

	void close(boolean forceRollback);

	boolean isClosed();

	void setExecutorWrapper(Executor executor);
}


