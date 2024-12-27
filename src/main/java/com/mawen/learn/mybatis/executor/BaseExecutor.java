package com.mawen.learn.mybatis.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mawen.learn.mybatis.cache.CacheKey;
import com.mawen.learn.mybatis.cache.impl.PerpetualCache;
import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.statement.StatementUtil;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import com.mawen.learn.mybatis.logging.jdbc.ConnectionLogger;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.ParameterMapping;
import com.mawen.learn.mybatis.mapping.ParameterMode;
import com.mawen.learn.mybatis.mapping.StatementType;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.LocalCacheScope;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;

import static com.mawen.learn.mybatis.executor.ExecutionPlaceholder.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/12
 */
public abstract class BaseExecutor implements Executor {

	private static final Log log = LogFactory.getLog(BaseExecutor.class);

	protected Transaction transaction;
	protected Executor wrapper;

	protected ConcurrentLinkedQueue<DeferredLoad> deferredLoads;
	/**
	 * 一级缓存
	 */
	protected PerpetualCache localCache;
	protected PerpetualCache localOutputParameterCache;
	/**
	 * 全局配置
	 */
	protected Configuration configuration;

	protected int queryStack;
	private boolean closed;

	protected BaseExecutor(Configuration configuration, Transaction transaction) {
		this.transaction = transaction;
		this.deferredLoads = new ConcurrentLinkedQueue<>();
		this.localCache = new PerpetualCache("LocalCache");
		this.localOutputParameterCache = new PerpetualCache("LocalOutputParameterCache");
		this.closed = false;
		this.configuration = configuration;
		this.wrapper = this;
	}


	@Override
	public int update(MappedStatement ms, Object parameter) throws SQLException {
		ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
		if (closed) {
			throw new ExecutorException("Executor was closed.");
		}
		clearLocalCache();
		return doUpdate(ms, parameter);
	}

	@Override
	public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
		ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
		if (closed) {
			throw new ExecutorException("Executor was closed.");
		}

		if (queryStack == 0 && ms.isFlushCacheRequired()) {
			clearLocalCache();
		}

		List<E> list;
		try {
			queryStack++;
			list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
			if (list != null) {
				log.debug("Get from Cache by key: " + key + " -> " + list);
				handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
			}
			else {
				list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
			}
		}
		finally {
			queryStack--;
		}

		if (queryStack == 0) {
			for (DeferredLoad deferredLoad : deferredLoads) {
				deferredLoad.load();
			}
			deferredLoads.clear();
			if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
				clearLocalCache();
			}
		}
		return list;
	}

	@Override
	public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
		BoundSql boundSql = ms.getBoundSql(parameter);
		CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
		return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
	}

	@Override
	public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
		BoundSql boundSql = ms.getBoundSql(parameter);
		return doQueryCursor(ms, parameter, rowBounds, boundSql);
	}

	@Override
	public List<BatchResult> flushStatements() throws SQLException {
		return flushStatements(false);
	}

	public List<BatchResult> flushStatements(boolean isRollback) throws SQLException {
		if (closed) {
			throw new ExecutorException("Executor was closed.");
		}
		return doFlushStatements(isRollback);
	}

	@Override
	public void commit(boolean required) throws SQLException {
		if (closed) {
			throw new ExecutorException("Cannot commit, transaction is already closed");
		}
		clearLocalCache();
		flushStatements();
		if (required) {
			transaction.commit();
		}
	}

	@Override
	public void rollback(boolean required) throws SQLException {
		if (!closed) {
			try {
				clearLocalCache();
				flushStatements(true);
			}
			finally {
				if (required) {
					transaction.rollback();
				}
			}
		}
	}

	@Override
	public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
		if (closed) {
			throw new ExecutorException("Executor was closed.");
		}

		CacheKey cacheKey = new CacheKey();
		cacheKey.update(ms.getId());
		cacheKey.update(rowBounds.getOffset());
		cacheKey.update(rowBounds.getLimit());
		cacheKey.update(boundSql.getSql());
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();

		for (ParameterMapping parameterMapping : parameterMappings) {
			if (parameterMapping.getMode() != ParameterMode.OUT) {
				Object value;
				String propertyName = parameterMapping.getProperty();
				if (boundSql.hasAdditionalParameter(propertyName)) {
					value = boundSql.getAdditionalParameter(propertyName);
				}
				else if (parameterObject == null) {
					value = null;
				}
				else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
					value = parameterObject;
				}
				else {
					MetaObject metaObject = configuration.newMetaObject(parameterObject);
					value = metaObject.getValue(propertyName);
				}
				cacheKey.update(value);
			}
		}

		if (configuration.getEnvironment() != null) {
			cacheKey.update(configuration.getEnvironment().getId());
		}
		return cacheKey;
	}

	@Override
	public boolean isCached(MappedStatement ms, CacheKey key) {
		return localCache.getObject(key) != null;
	}

	@Override
	public void clearLocalCache() {
		if (!closed) {
			localCache.clear();
			localOutputParameterCache.clear();
		}
	}

	@Override
	public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
		if (closed) {
			throw new ExecutorException("Executor was closed.");
		}

		DeferredLoad deferredLoad = new DeferredLoad(resultObject, property, key, localCache, configuration, targetType);
		if (deferredLoad.canLoad()) {
			deferredLoad.load();
		}
		else {
			deferredLoads.add(new DeferredLoad(resultObject, property, key, localCache, configuration, targetType));
		}
	}

	@Override
	public Transaction getTransaction() {
		if (closed) {
			throw new ExecutorException("Executor was closed");
		}
		return transaction;
	}

	@Override
	public void close(boolean forceRollback) {
		try {
			try {
				rollback(forceRollback);
			}
			finally {
				if (transaction != null) {
					transaction.close();
				}
			}
		}
		catch (SQLException e) {
			log.warn("Unexpected exception on closing transaction. Cause: " + e);
		}
		finally {
			transaction = null;
			deferredLoads = null;
			localCache = null;
			localOutputParameterCache = null;
			closed = true;
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	protected abstract int doUpdate(MappedStatement ms, Object parameter) throws SQLException;

	protected abstract List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException;

	protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException;

	protected abstract <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException;

	protected void closeStatement(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			}
			catch (SQLException e) {
				// ignored
			}
		}
	}

	protected void applyTransactionTimeout(Statement statement) throws SQLException {
		StatementUtil.applyTransactionTimeout(statement, statement.getQueryTimeout(), transaction.getTimeout());
	}

	private void handleLocallyCachedOutputParameters(MappedStatement ms, CacheKey key, Object parameter, BoundSql boundSql) {
		if (ms.getStatementType() == StatementType.CALLABLE) {
			final Object cachedParameter = localOutputParameterCache.getObject(key);
			if (cachedParameter != null && parameter != null) {
				final MetaObject metaCachedParameter = configuration.newMetaObject(cachedParameter);
				final MetaObject metaParameter = configuration.newMetaObject(parameter);
				for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
					if (parameterMapping.getMode() != ParameterMode.IN) {
						final String parameterName = parameterMapping.getProperty();
						final Object cachedValue = metaCachedParameter.getValue(parameterName);
						metaParameter.setValue(parameterName, cachedValue);
					}
				}
			}
		}
	}

	private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
		List<E> list;
		/**
		 * 在本地缓存中预留预占位
		 */
		localCache.putObject(key, EXECUTION_PLACEHOLDER);
		try {
			list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
		}
		finally {
			/**
			 * 从本地缓存中移除预占位
			 */
			localCache.removeObject(key);
		}

		/**
		 * 将查询结果写入本地缓存
		 */
		localCache.putObject(key, list);
		if (ms.getStatementType() == StatementType.CALLABLE) {
			localOutputParameterCache.putObject(key, parameter);
		}

		return list;
	}

	protected Connection getConnection(Log statementLog) throws SQLException {
		Connection connection = transaction.getConnection();
		if (statementLog.isDebugEnabled()) {
			return ConnectionLogger.newInstance(connection, statementLog, queryStack);
		}
		else {
			return connection;
		}
	}

	@Override
	public void setExecutorWrapper(Executor wrapper) {
		this.wrapper = wrapper;
	}

	private static class DeferredLoad {

		private final MetaObject resultObject;
		private final String property;
		private final Class<?> targetType;
		private final CacheKey key;
		private final PerpetualCache localCache;
		private final ObjectFactory objectFactory;
		private final ResultExtractor resultExtractor;

		public DeferredLoad(MetaObject resultObject, String property, CacheKey key, PerpetualCache localCache, Configuration configuration, Class<?> targetType) {
			this.resultObject = resultObject;
			this.property = property;
			this.key = key;
			this.localCache = localCache;
			this.objectFactory = configuration.getObjectFactory();
			this.resultExtractor = new ResultExtractor(configuration, objectFactory);
			this.targetType = targetType;
		}

		public boolean canLoad() {
			return localCache.getObject(key) != null && localCache.getObject(key) != EXECUTION_PLACEHOLDER;
		}

		public void load() {
			List<Object> list = (List<Object>) localCache.getObject(key);
			Object value = resultExtractor.extractObjectFromList(list, targetType);
			resultObject.setValue(property, value);
		}
	}
}
