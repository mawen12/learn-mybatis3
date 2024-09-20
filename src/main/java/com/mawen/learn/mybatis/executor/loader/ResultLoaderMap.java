package com.mawen.learn.mybatis.executor.loader;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.BaseExecutor;
import com.mawen.learn.mybatis.executor.BatchResult;
import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public class ResultLoaderMap {

	private final Map<String, LoadPair> loaderMap = new HashMap<>();

	public void addLoader(String property, MetaObject metaResultObject, ResultLoader resultLoader) {
		String upperFirst = getUppercaseFirstProperty(property);
		if (!upperFirst.equalsIgnoreCase(property) && loaderMap.containsKey(upperFirst)) {
			throw new ExecutorException("Nested lazy loaded result property '" + property + "' for query id '" + resultLoader.mappedStatement.getId()
			                            + " already exists in the result map. The leftmost property of all lazy loaded properties must be unique within a result map.");
		}
		loaderMap.put(upperFirst, new LoadPair(property, metaResultObject, resultLoader));
	}

	public final Map<String, LoadPair> getProperties() {
		return new HashMap<>(loaderMap);
	}

	public Set<String> getPropertyNames() {
		return loaderMap.keySet();
	}

	public int size() {
		return loaderMap.size();
	}

	public boolean hasLoader(String property) {
		return loaderMap.containsKey(property.toLowerCase(Locale.ENGLISH));
	}

	public boolean load(String property) throws SQLException {
		LoadPair pair = loaderMap.remove(property.toLowerCase(Locale.ENGLISH));
		if (pair != null) {
			pair.load();
			return true;
		}
		return false;
	}

	public void remove(String property) {
		loaderMap.remove(property.toUpperCase(Locale.ENGLISH));
	}

	public void loadAll() throws SQLException {
		final Set<String> methodNameSet = loaderMap.keySet();
		String[] methodNames = methodNameSet.toArray(new String[methodNameSet.size()]);
		for (String methodName : methodNames) {
			load(methodName);
		}
	}

	private static String getUppercaseFirstProperty(String property) {
		String[] parts = property.split("\\.");
		return parts[0].toUpperCase(Locale.ENGLISH);
	}

	public static class LoadPair implements Serializable {

		private static final long serialVersionUID = -41484871910017967L;

		private static final String FACTORY_METHOD = "getConfiguration";

		private final transient Object serializationCheck = new Object();

		private transient MetaObject metaResultObject;

		private transient ResultLoader resultLoader;

		private transient Log log;

		private Class<?> configurationFactory;

		private String property;

		private String mappedStatement;

		private Serializable mappedParameter;

		private LoadPair(final String property, MetaObject metaResultObject, ResultLoader resultLoader) {
			this.property = property;
			this.metaResultObject = metaResultObject;
			this.resultLoader = resultLoader;

			if (metaResultObject != null && metaResultObject.getOriginalObject() instanceof Serializable) {
				final Object mappedStatementParameter = resultLoader.parameterObject;

				if (mappedStatementParameter instanceof Serializable) {
					this.mappedStatement = resultLoader.mappedStatement.getId();
					this.mappedParameter = (Serializable) mappedStatementParameter;
					this.configurationFactory = resultLoader.configuration.getConfigurationFactory();
				}
				else {
					Log log = this.getLogger();
					if (log.isDebugEnabled()) {
						log.debug("Property [" + this.property + "] of [" + metaResultObject.getOriginalObject().getClass() + "] cannot be loaded after deserialization. " +
						          "Make sure it's loaded before serializing forenamed object.");
					}
				}
			}
		}

		public void load() throws SQLException {
			if (this.metaResultObject == null) {
				throw new IllegalArgumentException("metaResultObject is null");
			}

			if (this.resultLoader == null) {
				throw new IllegalArgumentException("resultLoader is null");
			}

			this.load(null);
		}

		public void load(final Object userObject) throws SQLException {
			if (this.metaResultObject == null || this.resultLoader == null) {
				if (this.mappedParameter == null) {
					throw new ExecutorException("Property [" + this.property + "] cannot be loaded because required parameter of mapped statement ["
					                            + this.mappedStatement + "] is not serializable");
				}

				final Configuration configuration = this.getConfiguration();
				final MappedStatement ms = configuration.getMappedStatement(this.mappedStatement);
				if (ms == null) {
					throw new ExecutorException("Cannot lazy load property [" + this.property + "] of deserialized object [" + userObject.getClass()
					                            + "] because configuration does not contain statement [" + this.mappedStatement + "]");
				}

				this.metaResultObject = configuration.newMetaObject(userObject);
				this.resultLoader = new ResultLoader(configuration, new ClosedExecutor(), ms, this.mappedParameter, metaResultObject.getSetterType(this.property), null, null);
			}

			if (this.serializationCheck == null) {
				final ResultLoader old = this.resultLoader;
				this.resultLoader = new ResultLoader(old.configuration, new ClosedExecutor(), old.mappedStatement, old.parameterObject, old.targetType, old.cacheKey, old.boundSql);
			}

			this.metaResultObject.setValue(property,this.resultLoader.loadResult());
		}

		private Configuration getConfiguration() {
			if (this.configurationFactory == null) {
				throw new ExecutorException("Cannot get Configuration as configuration factory was not set.");
			}

			Object configurationObject;
			try {
				final Method factoryMethod = this.configurationFactory.getDeclaredMethod(FACTORY_METHOD);
				if (!Modifier.isStatic(factoryMethod.getModifiers())) {
					throw new ExecutorException("Cannot get Configuration as factory method [" + this.configurationFactory + "]#[" + FACTORY_METHOD + "] is not static.");
				}

				if (!factoryMethod.isAccessible()) {
					configurationObject = AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
						try {
							factoryMethod.setAccessible(true);
							return factoryMethod.invoke(this);
						}
						finally {
							factoryMethod.setAccessible(false);
						}
					});
				}
				else {
					configurationObject = factoryMethod.invoke(null);
				}
			}
			catch (final ExecutorException e) {
				throw e;
			}
			catch (final NoSuchMethodException e) {
				throw new ExecutorException("Cannot get Configuration as factory class [" + this.configurationFactory + "] is missing factory method of name ["
				                            + FACTORY_METHOD + "].", e);
			}
			catch (final PrivilegedActionException e) {
				throw new ExecutorException("Cannot get Configuration as factory method [" + this.configurationFactory + "]#[" + FACTORY_METHOD + "] threw an exception.", e.getCause());
			}
			catch (final Exception e) {
				throw new ExecutorException("Cannot get Configuration as factory method [" + this.configurationFactory + "]#[" + FACTORY_METHOD + "] threw an exception.", e);
			}

			if (!(configurationObject instanceof Configuration)) {
				throw new ExecutorException("Cannot get Configuration as factory method [" + this.configurationFactory + "]#[" + FACTORY_METHOD + "] didn't return [" + Configuration.class
				                            + "] but [" + (configurationObject == null ? "null" : configurationObject.getClass()) + "].");
			}

			return Configuration.class.cast(configurationObject);
		}

		private Log getLogger() {
			if (this.log == null) {
				this.log = LogFactory.getLog(this.getClass());
			}
			return this.log;
		}
	}

	private static final class ClosedExecutor extends BaseExecutor {

		public ClosedExecutor() {
			super(null, null);
		}

		@Override
		protected int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
			throw new UnsupportedOperationException("Not supported.");
		}

		@Override
		protected List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
			throw new UnsupportedOperationException("Not supported.");
		}

		@Override
		protected <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
			throw new UnsupportedOperationException("Not supported.");
		}

		@Override
		protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
			throw new UnsupportedOperationException("Not supported.");
		}
	}
}
