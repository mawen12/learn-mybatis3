package com.mawen.learn.mybatis.session;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.mawen.learn.mybatis.io.VFS;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.mapping.Environment;
import com.mawen.learn.mybatis.mapping.ResultSetType;
import com.mawen.learn.mybatis.reflection.DefaultReflectorFactory;
import com.mawen.learn.mybatis.reflection.ReflectorFactory;
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
}
