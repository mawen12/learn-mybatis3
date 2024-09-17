package com.mawen.learn.mybatis.session;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.mawen.learn.mybatis.executor.loader.ProxyFactory;
import com.mawen.learn.mybatis.executor.loader.javassist.JavassistProxyFactory;
import com.mawen.learn.mybatis.io.VFS;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.mapping.Environment;
import com.mawen.learn.mybatis.mapping.ResultSetType;
import com.mawen.learn.mybatis.reflection.DefaultReflectorFactory;
import com.mawen.learn.mybatis.reflection.ReflectorFactory;
import com.mawen.learn.mybatis.reflection.factory.DefaultObjectFactory;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.mawen.learn.mybatis.type.JdbcType;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class Configuration {

	protected Environment environment;

	protected boolean safeRowBoundsEnabled;
	protected boolean safeResultHandlerEnabled = true;
	protected boolean mapUnderscoreToCamelCase;
	protected boolean aggressiveLazyLoading;
	protected boolean multipleResultSetsEnabled = true;
	protected boolean useGeneratedKeys;
	protected boolean useColumnLabel = true;
	protected boolean cacheEnabled = true;
	protected boolean callSettersOnNulls;
	protected boolean useActualParamName = true;
	protected boolean returnInstanceForEmptyRow;
	protected boolean shrinkWhitespacesInSql;
	protected boolean nullableOnForEach;
	protected boolean argNameBasedConstructorAutoMapping;

	protected String logPrefix;
	protected Class<? extends Log> logImpl;
	protected Class<? extends VFS> vfsImpl;
	protected Class<?> defaultSqlProviderType;
	protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;
	protected JdbcType jdbcTypeForNull = JdbcType.OTHER;
	protected Set<String> lazyLoadTriggerMethods = new HashSet<>(Arrays.asList("equals", "hashCode", "toString", "clone"));
	protected Integer defaultStatementTimeout;
	protected Integer defaultFetchSize;
	protected ResultSetType defaultResultSetType;
	protected ExecutorType defaultExecutorType = ExecutorType.SIMPLE;
	protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;
	protected AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior = AutoMappingUnknownColumnBehavior.NONE;

	protected Properties properties = new Properties();
	protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
	protected ObjectFactory objectFactory = new DefaultObjectFactory();
	protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

	protected boolean lazyLoadingEnabled = false;
	protected ProxyFactory proxyFactory = new JavassistProxyFactory();

	protected static class StrictMap<V> extends HashMap<String, V> {

		private static final long serialVersionUID = -6970800884001334580L;

		private final String name;

	}
}
