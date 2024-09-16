package com.mawen.learn.mybatis.session;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.BatchResult;
import com.mawen.learn.mybatis.reflection.ExceptionUtil;
import sun.jvm.hotspot.debugger.ReadResult;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class SqlSessionManager implements SqlSessionFactory, SqlSession {

	private final SqlSessionFactory sqlSessionFactory;
	private final SqlSession sqlSessionProxy;

	private final ThreadLocal<SqlSession> localSqlSession = new ThreadLocal<>();

	private SqlSessionManager(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
		this.sqlSessionProxy = (SqlSession) Proxy.newProxyInstance(
				SqlSessionFactory.class.getClassLoader(),
				new Class[] {SqlSession.class},
				new SqlSessionInterceptor());
	}

	public static SqlSessionManager newInstance(Reader reader) {
		return new SqlSessionManager(new SqlSessionFactoryBuilder().build(reader, null, null));
	}

	public static SqlSessionManager newInstance(Reader reader, String environment) {
		return new SqlSessionManager(new SqlSessionFactoryBuilder().build(reader, environment, null));
	}

	public static SqlSessionManager newInstance(Reader reader, String environment, Properties properties) {
		return new SqlSessionManager(new SqlSessionFactoryBuilder().build(reader, environment, properties));
	}

	public static SqlSessionManager newInstance(InputStream inputStream) {
		return new SqlSessionManager(new SqlSessionFactoryBuilder().build(inputStream, null, null));
	}

	public static SqlSessionManager newInstance(InputStream inputStream, String environment) {
		return new SqlSessionManager(new SqlSessionFactoryBuilder().build(inputStream, environment, null));
	}

	public static SqlSessionManager newInstance(InputStream inputStream, String environment, Properties properties) {
		return new SqlSessionManager(new SqlSessionFactoryBuilder().build(inputStream, environment, properties));
	}

	public static SqlSessionManager newInstance(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionManager(sqlSessionFactory);
	}

	public void startManagedSession() {
		this.localSqlSession.set(openSession());
	}

	public void startManagedSession(boolean autoCommit) {
		this.localSqlSession.set(openSession(autoCommit));
	}

	public void setManagedSession(Connection connection) {
		this.localSqlSession.set(openSession(connection));
	}

	@Override
	public <T> T selectOne(String statement) {
		return null;
	}

	@Override
	public <T> T selectOne(String statement, Object parameter) {
		return null;
	}

	@Override
	public <E> List<E> selectList(String statement) {
		return List.of();
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter) {
		return List.of();
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
		return List.of();
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
		return Map.of();
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
		return Map.of();
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
		return Map.of();
	}

	@Override
	public <T> Cursor<T> selectCursor(String statement) {
		return null;
	}

	@Override
	public <T> Cursor<T> selectCursor(String statement, Object parameter) {
		return null;
	}

	@Override
	public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
		return null;
	}

	@Override
	public void select(String statement, Object parameter, ResultHandler handler) {

	}

	@Override
	public void select(String statement, ResultHandler handler) {

	}

	@Override
	public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {

	}

	@Override
	public int insert(String statement) {
		return 0;
	}

	@Override
	public int insert(String statement, Object parameter) {
		return 0;
	}

	@Override
	public int update(String statement) {
		return 0;
	}

	@Override
	public int update(String statement, Object parameter) {
		return 0;
	}

	@Override
	public int delete(String statement) {
		return 0;
	}

	@Override
	public int delete(String statement, Object parameter) {
		return 0;
	}

	@Override
	public void commit() {

	}

	@Override
	public void commit(boolean force) {

	}

	@Override
	public void rollback() {

	}

	@Override
	public void rollback(boolean force) {

	}

	@Override
	public List<BatchResult> flushStatements() {
		return List.of();
	}

	@Override
	public void close() {

	}

	@Override
	public void clearCache() {

	}

	@Override
	public <T> T getMapper(Class<T> type) {
		return null;
	}

	@Override
	public Connection getConnection() {
		return null;
	}

	@Override
	public SqlSession openSession() {
		return null;
	}

	@Override
	public SqlSession openSession(boolean autoCommit) {
		return null;
	}

	@Override
	public SqlSession openSession(Connection connection) {
		return null;
	}

	@Override
	public SqlSession openSession(TransactionIsolationLevel level) {
		return null;
	}

	@Override
	public SqlSession openSession(ExecutorType execType) {
		return null;
	}

	@Override
	public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
		return null;
	}

	@Override
	public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
		return null;
	}

	@Override
	public SqlSession openSession(ExecutorType execType, Connection connection) {
		return null;
	}

	@Override
	public Configuration getConfiguration() {
		return null;
	}

	private class SqlSessionInterceptor implements InvocationHandler {

		public SqlSessionInterceptor() {}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			final SqlSession sqlSession = SqlSessionManager.this.localSqlSession.get();
			if (sqlSession != null) {
				try {
					return method.invoke(sqlSession, args);
				}
				catch (Throwable t) {
					throw ExceptionUtil.unwrapThrowable(t);
				}
			}
			else {
				try (SqlSession autoSqlSession = openSession()) {
					try {
						final Object result = method.invoke(autoSqlSession, args);
						autoSqlSession.commit();
						return result;
					}
					catch (Throwable t) {
						autoSqlSession.rollback();
						throw ExceptionUtil.unwrapThrowable(t);
					}
				}
			}
		}
	}
}
