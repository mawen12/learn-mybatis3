package com.mawen.learn.mybatis.executor.loader;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.cache.CacheKey;
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.executor.ResultExtractor;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.Environment;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ExecutorType;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.transaction.TransactionFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public class ResultLoader {

	protected final Configuration configuration;
	protected final Executor executor;
	protected final MappedStatement mappedStatement;
	protected final Object parameterObject;
	protected final Class<?> targetType;
	protected final ObjectFactory objectFactory;
	protected final CacheKey cacheKey;
	protected final BoundSql boundSql;
	protected final ResultExtractor resultExtractor;
	protected final long creatorThreadId;

	protected boolean loaded;
	protected Object resultObject;

	public ResultLoader(Configuration configuration, Executor executor, MappedStatement mappedStatement, Object parameterObject, Class<?> targetType, CacheKey cacheKey, BoundSql boundSql) {
		this.configuration = configuration;
		this.executor = executor;
		this.mappedStatement = mappedStatement;
		this.parameterObject = parameterObject;
		this.targetType = targetType;
		this.objectFactory = configuration.getObjectFactory();
		this.cacheKey = cacheKey;
		this.boundSql = boundSql;
		this.resultExtractor = new ResultExtractor(configuration,objectFactory);
		this.creatorThreadId = Thread.currentThread().getId();
	}

	public Object loadResult() throws SQLException {
		List<Object> list = selectList();
		resultObject = resultExtractor.extractObjectFromList(list, targetType);
		return resultObject;
	}

	private <E> List<E> selectList() throws SQLException {
		Executor localExecutor = executor;
		if (Thread.currentThread().getId() != this.creatorThreadId || localExecutor.isClosed()) {
			localExecutor = newExecutor();
		}

		try {
			return localExecutor.query(mappedStatement, parameterObject, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, cacheKey, boundSql);
		}
		finally {
			if (localExecutor != executor) {
				localExecutor.close(false);
			}
		}
	}

	private Executor newExecutor() {
		final Environment environment = configuration.getEnvironment();
		if (environment == null) {
			throw new ExecutorException("ResultLoader could not load lazily. Environment was not configured.");
		}

		final DataSource ds = environment.getDataSource();
		if (ds == null) {
			throw new ExecutorException("ResultLoader could not load lazily. DataSource was not configured.");
		}

		final TransactionFactory transactionFactory = environment.getTransactionFactory();
		final Transaction tx = transactionFactory.newTransaction(ds, null, false);
		return configuration.newExecutor(tx, ExecutorType.SIMPLE);
	}

	public boolean wasNull() {
		return resultObject == null;
	}
}
