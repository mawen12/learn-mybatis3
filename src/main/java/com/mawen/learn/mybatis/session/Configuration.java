package com.mawen.learn.mybatis.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;

import com.mawen.learn.mybatis.binding.MapperRegistry;
import com.mawen.learn.mybatis.builder.CacheRefResolver;
import com.mawen.learn.mybatis.builder.ResultMapResolver;
import com.mawen.learn.mybatis.builder.annotation.MethodResolver;
import com.mawen.learn.mybatis.builder.xml.XMLStatementBuilder;
import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.cache.decorators.FifoCache;
import com.mawen.learn.mybatis.cache.decorators.LruCache;
import com.mawen.learn.mybatis.cache.decorators.SoftCache;
import com.mawen.learn.mybatis.cache.decorators.WeakCache;
import com.mawen.learn.mybatis.cache.impl.PerpetualCache;
import com.mawen.learn.mybatis.datasource.jndi.JndiDataSourceFactory;
import com.mawen.learn.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.mawen.learn.mybatis.datasource.unpooled.UnpooledDataSource;
import com.mawen.learn.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.mawen.learn.mybatis.executor.BatchExecutor;
import com.mawen.learn.mybatis.executor.CachingExecutor;
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.ReuseExecutor;
import com.mawen.learn.mybatis.executor.SimpleExecutor;
import com.mawen.learn.mybatis.executor.keygen.KeyGenerator;
import com.mawen.learn.mybatis.executor.loader.ProxyFactory;
import com.mawen.learn.mybatis.executor.loader.cglib.CglibProxyFactory;
import com.mawen.learn.mybatis.executor.loader.javassist.JavassistProxyFactory;
import com.mawen.learn.mybatis.executor.parameter.ParameterHandler;
import com.mawen.learn.mybatis.executor.resultset.DefaultResultSetHandler;
import com.mawen.learn.mybatis.executor.resultset.ResultSetHandler;
import com.mawen.learn.mybatis.executor.statement.RoutingStatementHandler;
import com.mawen.learn.mybatis.executor.statement.StatementHandler;
import com.mawen.learn.mybatis.io.VFS;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.Environment;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.ParameterMap;
import com.mawen.learn.mybatis.mapping.ResultMap;
import com.mawen.learn.mybatis.mapping.ResultSetType;
import com.mawen.learn.mybatis.mapping.VendorDatabaseIdProvider;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.plugin.Interceptor;
import com.mawen.learn.mybatis.plugin.InterceptorChain;
import com.mawen.learn.mybatis.reflection.DefaultReflectorFactory;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.ReflectorFactory;
import com.mawen.learn.mybatis.reflection.factory.DefaultObjectFactory;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.mawen.learn.mybatis.scripting.LanguageDriver;
import com.mawen.learn.mybatis.scripting.LanguageDriverRegistry;
import com.mawen.learn.mybatis.scripting.defaults.RawLanguageDriver;
import com.mawen.learn.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.mawen.learn.mybatis.transaction.managed.ManagedTransactionFactory;
import com.mawen.learn.mybatis.type.EnumTypeHandler;
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.TypeAliasRegistry;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;
import com.sun.tools.jdi.RawCommandLineLauncher;
import jdk.vm.ci.code.site.ExceptionHandler;

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

	protected Properties variables = new Properties();
	protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
	protected ObjectFactory objectFactory = new DefaultObjectFactory();
	protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

	protected boolean lazyLoadingEnabled = false;
	protected ProxyFactory proxyFactory = new JavassistProxyFactory();

	protected String databaseId;

	protected Class<?> configurationFactory;

	protected final MapperRegistry mapperRegistry = new MapperRegistry(this);
	protected final InterceptorChain interceptorChain = new InterceptorChain();
	protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry(this);
	protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
	protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

	protected final Map<String, MappedStatement> mappedStatements = new StrictMap<MappedStatement>("Mapped Statements collection")
			.conflictMessageProducer((savedValue, targetValue) -> ". please check " + savedValue.getResource() + " and " + targetValue.getResource());
	protected final Map<String, Cache> caches = new StrictMap<>("Caches collection");
	protected final Map<String, ResultMap> resultMaps = new StrictMap<>("Result Maps collection");
	protected final Map<String, ParameterMap> parameterMaps = new StrictMap<>("Parameter Maps collection");
	protected final Map<String, KeyGenerator> keyGenerators = new StrictMap<>("Key Generators collection");

	protected final Set<String> loadResources = new HashSet<>();
	protected final Map<String, XNode> sqlFragments = new StrictMap<>("XML fragments parsed from previous mappers");

	protected final Collection<XMLStatementBuilder> incompleteStatements = new LinkedList<>();
	protected final Collection<CacheRefResolver> incompleteCacheRefs = new LinkedList<>();
	protected final Collection<ResultMapResolver> incompleteResultMaps = new LinkedList<>();
	protected final Collection<MethodResolver> incompleteMethods = new LinkedList<>();

	protected final Map<String, String> cacheRefMap = new HashMap<>();

	public Configuration(Environment environment) {
		this();
		this.environment = environment;
	}

	public Configuration() {
		typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
		typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class);

		typeAliasRegistry.registerAlias("JNDI", JndiDataSourceFactory.class);
		typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
		typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);

		typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
		typeAliasRegistry.registerAlias("FIFO", FifoCache.class);
		typeAliasRegistry.registerAlias("LRU", LruCache.class);
		typeAliasRegistry.registerAlias("SOFT", SoftCache.class);
		typeAliasRegistry.registerAlias("WEAK", WeakCache.class);

		typeAliasRegistry.registerAlias("DB_VENDOR", VendorDatabaseIdProvider.class);

		typeAliasRegistry.registerAlias("XML", XMLLanguageDriver.class);
		typeAliasRegistry.registerAlias("RAW", RawCommandLineLauncher.class);

		typeAliasRegistry.registerAlias("SLF4J", Slf4jImpl.class);
		typeAliasRegistry.registerAlias("COMMONS_LOGGING", JakartaCommonsLoggingImpl.class);
		typeAliasRegistry.registerAlias("LOG4J", Log4jImpl.class);
		typeAliasRegistry.registerAlias("LOG4J2", Log4j2Impl.class);
		typeAliasRegistry.registerAlias("JDK_LOGGING", Jdk14LoggingImpl.class);
		typeAliasRegistry.registerAlias("STDOUT_LOGGING", StdOutImpl.class);
		typeAliasRegistry.registerAlias("NO_LOGGING", NoLoggingImpl.class);

		typeAliasRegistry.registerAlias("CGLIB", CglibProxyFactory.class);
		typeAliasRegistry.registerAlias("JAVASSIST", JavassistProxyFactory.class);

		languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
		languageRegistry.register(RawLanguageDriver.class);
	}

	public String getLogPrefix() {
		return logPrefix;
	}

	public void setLogPrefix(String logPrefix) {
		this.logPrefix = logPrefix;
	}

	public Class<? extends Log> getLogImpl() {
		return logImpl;
	}

	public void setLogImpl(Class<? extends Log> logImpl) {
		if (logImpl != null) {
			this.logImpl = logImpl;
			LogFactory.useCustomLogging(this.logImpl);
		}
	}

	public Class<? extends VFS> getVfsImpl() {
		return vfsImpl;
	}

	public void setVfsImpl(Class<? extends VFS> vfsImpl) {
		if (vfsImpl != null) {
			this.vfsImpl = vfsImpl;
			VFS.addImplClass(this.vfsImpl);
		}
	}

	public Class<?> getDefaultSqlProviderType() {
		return defaultSqlProviderType;
	}

	public void setDefaultSqlProviderType(Class<?> defaultSqlProviderType) {
		this.defaultSqlProviderType = defaultSqlProviderType;
	}

	public boolean isCallSettersOnNulls() {
		return callSettersOnNulls;
	}

	public void setCallSettersOnNulls(boolean callSettersOnNulls) {
		this.callSettersOnNulls = callSettersOnNulls;
	}

	public boolean isUseActualParamName() {
		return useActualParamName;
	}

	public void setUseActualParamName(boolean useActualParamName) {
		this.useActualParamName = useActualParamName;
	}

	public boolean isReturnInstanceForEmptyRow() {
		return returnInstanceForEmptyRow;
	}

	public void setReturnInstanceForEmptyRow(boolean returnInstanceForEmptyRow) {
		this.returnInstanceForEmptyRow = returnInstanceForEmptyRow;
	}

	public boolean isShrinkWhitespacesInSql() {
		return shrinkWhitespacesInSql;
	}

	public void setShrinkWhitespacesInSql(boolean shrinkWhitespacesInSql) {
		this.shrinkWhitespacesInSql = shrinkWhitespacesInSql;
	}

	public boolean isNullableOnForEach() {
		return nullableOnForEach;
	}

	public void setNullableOnForEach(boolean nullableOnForEach) {
		this.nullableOnForEach = nullableOnForEach;
	}

	public boolean isArgNameBasedConstructorAutoMapping() {
		return argNameBasedConstructorAutoMapping;
	}

	public void setArgNameBasedConstructorAutoMapping(boolean argNameBasedConstructorAutoMapping) {
		this.argNameBasedConstructorAutoMapping = argNameBasedConstructorAutoMapping;
	}

	public String getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(String databaseId) {
		this.databaseId = databaseId;
	}

	public Class<?> getConfigurationFactory() {
		return configurationFactory;
	}

	public void setConfigurationFactory(Class<?> configurationFactory) {
		this.configurationFactory = configurationFactory;
	}

	public boolean isSafeResultHandlerEnabled() {
		return safeResultHandlerEnabled;
	}

	public void setSafeResultHandlerEnabled(boolean safeResultHandlerEnabled) {
		this.safeResultHandlerEnabled = safeResultHandlerEnabled;
	}

	public boolean isSafeRowBoundsEnabled() {
		return safeRowBoundsEnabled;
	}

	public void setSafeRowBoundsEnabled(boolean safeRowBoundsEnabled) {
		this.safeRowBoundsEnabled = safeRowBoundsEnabled;
	}

	public boolean isMapUnderscoreToCamelCase() {
		return mapUnderscoreToCamelCase;
	}

	public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
		this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
	}

	public void addLoadedResource(String resource) {
		loadResources.add(resource);
	}

	public boolean isLoadResources(String resource) {
		return loadResources.contains(resource);
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public AutoMappingBehavior getAutoMappingBehavior() {
		return autoMappingBehavior;
	}

	public void setAutoMappingBehavior(AutoMappingBehavior autoMappingBehavior) {
		this.autoMappingBehavior = autoMappingBehavior;
	}

	public AutoMappingUnknownColumnBehavior getAutoMappingUnknownColumnBehavior() {
		return autoMappingUnknownColumnBehavior;
	}

	public void setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior) {
		this.autoMappingUnknownColumnBehavior = autoMappingUnknownColumnBehavior;
	}

	public boolean isLazyLoadingEnabled() {
		return lazyLoadingEnabled;
	}

	public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
		this.lazyLoadingEnabled = lazyLoadingEnabled;
	}

	public ProxyFactory getProxyFactory() {
		return proxyFactory;
	}

	public void setProxyFactory(ProxyFactory proxyFactory) {
		if (proxyFactory == null) {
			proxyFactory = new JavassistProxyFactory();
		}
		this.proxyFactory = proxyFactory;
	}

	public boolean isAggressiveLazyLoading() {
		 return aggressiveLazyLoading;
	}

	public void setAggressiveLazyLoading(boolean aggressiveLazyLoading) {
		this.aggressiveLazyLoading = aggressiveLazyLoading;
	}

	public boolean isMultipleResultSetsEnabled() {
		return multipleResultSetsEnabled;
	}

	public void setMultipleResultSetsEnabled(boolean multipleResultSetsEnabled) {
		this.multipleResultSetsEnabled = multipleResultSetsEnabled;
	}

	public Set<String> getLazyLoadTriggerMethods() {
		return lazyLoadTriggerMethods;
	}

	public void setLazyLoadTriggerMethods(Set<String> lazyLoadTriggerMethods) {
		this.lazyLoadTriggerMethods = lazyLoadTriggerMethods;
	}

	public boolean isUseGeneratedKeys() {
		return useGeneratedKeys;
	}

	public void setUseGeneratedKeys(boolean useGeneratedKeys) {
		this.useGeneratedKeys = useGeneratedKeys;
	}

	public ExecutorType getExecutorType() {
		return defaultExecutorType;
	}

	public void setExecutorType(ExecutorType defaultExecutorType) {
		this.defaultExecutorType = defaultExecutorType;
	}

	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}

	public Integer getDefaultStatementTimeout() {
		return defaultStatementTimeout;
	}

	public void setDefaultStatementTimeout(Integer defaultStatementTimeout) {
		this.defaultStatementTimeout = defaultStatementTimeout;
	}

	public Integer getDefaultFetchSize() {
		return defaultFetchSize;
	}

	public void setDefaultFetchSize(Integer defaultFetchSize) {
		this.defaultFetchSize = defaultFetchSize;
	}

	public ResultSetType getDefaultResultSetType() {
		return defaultResultSetType;
	}

	public void setDefaultResultSetType(ResultSetType defaultResultSetType) {
		this.defaultResultSetType = defaultResultSetType;
	}

	public boolean isUseColumnLabel() {
		return useColumnLabel;
	}

	public void setUseColumnLabel(boolean useColumnLabel) {
		this.useColumnLabel = useColumnLabel;
	}

	public LocalCacheScope getLocalCacheScope() {
		return localCacheScope;
	}

	public void setLocalCacheScope(LocalCacheScope localCacheScope) {
		this.localCacheScope = localCacheScope;
	}

	public JdbcType getJdbcTypeForNull() {
		return jdbcTypeForNull;
	}

	public void setJdbcTypeForNull(JdbcType jdbcTypeForNull) {
		this.jdbcTypeForNull = jdbcTypeForNull;
	}

	public Properties getVariables() {
		return variables;
	}

	public void setVariables(Properties variables) {
		this.variables = variables;
	}

	public TypeHandlerRegistry getTypeHandlerRegistry() {
		return typeHandlerRegistry;
	}

	public void setDefaultEnumTypeHandler(Class<? extends TypeHandler> typeHandler) {
		if (typeHandler != null) {
			getTypeHandlerRegistry().setDefaultEnumTypeHandler(typeHandler);
		}
	}

	public TypeAliasRegistry getTypeAliasRegistry() {
		return typeAliasRegistry;
	}

	public MapperRegistry getMapperRegistry() {
		return mapperRegistry;
	}

	public ReflectorFactory getReflectorFactory() {
		return reflectorFactory;
	}

	public void setReflectorFactory(ReflectorFactory reflectorFactory) {
		this.reflectorFactory = reflectorFactory;
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public ObjectWrapperFactory getObjectWrapperFactory() {
		return objectWrapperFactory;
	}

	public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory) {
		this.objectWrapperFactory = objectWrapperFactory;
	}

	public List<Interceptor> getInterceptors() {
		return interceptorChain.getInterceptors();
	}

	public LanguageDriverRegistry getLanguageRegistry() {
		return languageRegistry;
	}

	public void setDefaultScriptingLanguage(Class<? extends LanguageDriver> driver) {
		if (driver == null) {
			driver = XMLLanguageDriver.class;
		}
		getLanguageRegistry().setDefaultDriverClass(driver);
	}

	public LanguageDriver getDefaultScriptingLanguageInstance() {
		return languageRegistry.getDefaultDriver();
	}

	public MetaObject newMetaObject(Object object) {
		return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
	}

	public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
		ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
		parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
		return parameterHandler;
	}

	public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql) {
		ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
		resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
		return resultSetHandler;
	}

	public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
		StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
		statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
		return statementHandler;
	}

	public Executor newExecutor(Transaction transaction) {
		return newExecutor(transaction, defaultExecutorType);
	}

	public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
		executorType = executorType == null ? defaultExecutorType : executorType;
		executorType = executorType == null ? ExecutorType.SIMPLE : executorType;

		Executor executor;
		if (ExecutorType.BATCH == executorType) {
			executor = new BatchExecutor(this, transaction);
		}
		else if (ExecutorType.REUSE == executorType) {
			executor = new ReuseExecutor(this, transaction);
		}
		else {
			executor = new SimpleExecutor(this, transaction);
		}

		if (cacheEnabled) {
			executor = new CachingExecutor(executor);
		}

		executor = (Executor) interceptorChain.pluginAll(executor);
		return executor;
	}

	public void addKeyGenerator(String id, KeyGenerator keyGenerator) {
		keyGenerators.put(id, keyGenerator);
	}

	protected void checkLocallyForDiscriminatedNestedResultMaps(ResultMap rm) {
		if (!rm.hasNestedResultMaps() && rm.getDiscriminator() != null) {
			for (Map.Entry<String, String> entry : rm.getDiscriminator().getDiscriminatorMap().entrySet()) {
				String discriminatedResultMapName = entry.getValue();
				if (hasResultMap(discriminatedResultMapName)) {
					resultMaps
				}
			}
		}
	}

	protected static class StrictMap<V> extends HashMap<String, V> {

		private static final long serialVersionUID = -6970800884001334580L;

		private final String name;
		private BiFunction<V, V, String> conflictMessageProducer;

		public StrictMap(String name, int initialCapacity, float loadFactor) {
			super(initialCapacity, loadFactor);
			this.name = name;
		}

		public StrictMap(String name, int initialCapacity) {
			super(initialCapacity);
			this.name = name;
		}

		public StrictMap(String name) {
			super();
			this.name = name;
		}

		public StrictMap(String name, Map<String, ? extends V> m) {
			super(m);
			this.name = name;
		}

		private StrictMap<V> conflictMessageProducer(BiFunction<V, V, String> conflictMessageProducer) {
			this.conflictMessageProducer = conflictMessageProducer;
			return this;
		}

		@Override
		public V put(String key, V value) {
			if (containsKey(key)) {
				throw new IllegalArgumentException(name + " already contains value for " + key + (conflictMessageProducer == null ? "" : conflictMessageProducer.apply(super.get(key), value)));
			}
			if (key.contains(".")) {
				String shortKey = getShortName(key);
				if (super.get(shortKey) == null) {
					super.put(shortKey, value);
				}
				else {
					super.put(shortKey, (V) new Ambiguity(shortKey));
				}
			}
			return super.put(key, value);
		}

		@Override
		public V get(Object key) {
			V value = super.get(key);
			if (value == null) {
				throw new IllegalArgumentException(name + " does not contain value for " + key);
			}
			if (value instanceof Ambiguity) {
				throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
				                                   + " (try using the full name including the namespace, or rename one of the entries) ");
			}
			return value;
		}

		private String getShortName(String key) {
			String[] parts = key.split("\\.");
			return parts[parts.length - 1];
		}

		protected static class Ambiguity {

			private final String subject;

			public Ambiguity(String subject) {
				this.subject = subject;
			}

			public String getSubject() {
				return subject;
			}

		}
	}
}
