package com.mawen.learn.mybatis.session;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;

import com.mawen.learn.mybatis.binding.MapperRegistry;
import com.mawen.learn.mybatis.builder.CacheRefResolver;
import com.mawen.learn.mybatis.builder.IncompleteElementException;
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
import com.mawen.learn.mybatis.logging.commons.JakartaCommonsLoggingImpl;
import com.mawen.learn.mybatis.logging.jdk14.Jdk14LoggingImpl;
import com.mawen.learn.mybatis.logging.log4j2.Log4j2Impl;
import com.mawen.learn.mybatis.logging.nologging.NoLoggingImpl;
import com.mawen.learn.mybatis.logging.slf4j.Slf4jImpl;
import com.mawen.learn.mybatis.logging.stdout.StdOutImpl;
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
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.TypeAliasRegistry;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class Configuration {

	@Setter
	@Getter
	protected Environment environment;

	@Setter
	@Getter
	protected boolean safeRowBoundsEnabled;
	@Setter
	@Getter
	protected boolean safeResultHandlerEnabled = true;
	@Setter
	@Getter
	protected boolean mapUnderscoreToCamelCase;
	@Setter
	@Getter
	protected boolean aggressiveLazyLoading;
	@Setter
	@Getter
	protected boolean multipleResultSetsEnabled = true;
	@Setter
	@Getter
	protected boolean useGeneratedKeys;
	@Setter
	@Getter
	protected boolean useColumnLabel = true;
	@Setter
	@Getter
	protected boolean cacheEnabled = true;
	@Setter
	@Getter
	protected boolean callSettersOnNulls;
	@Setter
	@Getter
	protected boolean useActualParamName = true;
	@Setter
	@Getter
	protected boolean returnInstanceForEmptyRow;
	@Setter
	@Getter
	protected boolean shrinkWhitespacesInSql;
	@Setter
	@Getter
	protected boolean nullableOnForEach;
	@Setter
	@Getter
	protected boolean argNameBasedConstructorAutoMapping;

	@Setter
	@Getter
	protected String logPrefix;
	@Getter
	protected Class<? extends Log> logImpl;
	@Getter
	protected Class<? extends VFS> vfsImpl;
	@Setter
	@Getter
	protected Class<?> defaultSqlProviderType;
	@Setter
	@Getter
	protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;
	@Setter
	@Getter
	protected JdbcType jdbcTypeForNull = JdbcType.OTHER;
	@Setter
	@Getter
	protected Set<String> lazyLoadTriggerMethods = new HashSet<>(Arrays.asList("equals", "hashCode", "toString", "clone"));
	@Setter
	@Getter
	protected Integer defaultStatementTimeout;
	@Setter
	@Getter
	protected Integer defaultFetchSize;
	@Setter
	@Getter
	protected ResultSetType defaultResultSetType;
	@Setter
	@Getter
	protected ExecutorType defaultExecutorType = ExecutorType.SIMPLE;
	@Setter
	@Getter
	protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;
	@Setter
	@Getter
	protected AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior = AutoMappingUnknownColumnBehavior.NONE;

	@Setter
	@Getter
	protected Properties variables = new Properties();
	@Setter
	@Getter
	protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
	@Setter
	@Getter
	protected ObjectFactory objectFactory = new DefaultObjectFactory();
	@Setter
	@Getter
	protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

	@Setter
	@Getter
	protected boolean lazyLoadingEnabled = false;
	@Getter
	protected ProxyFactory proxyFactory = new JavassistProxyFactory();

	@Setter
	@Getter
	protected String databaseId;

	@Setter
	@Getter
	protected Class<?> configurationFactory;

	@Getter
	protected final MapperRegistry mapperRegistry = new MapperRegistry(this);
	protected final InterceptorChain interceptorChain = new InterceptorChain();
	@Getter
	protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry(this);
	@Getter
	protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
	@Getter
	protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

	protected final Map<String, MappedStatement> mappedStatements = new StrictMap<MappedStatement>("Mapped Statements collection")
			.conflictMessageProducer((savedValue, targetValue) -> ". please check " + savedValue.getResource() + " and " + targetValue.getResource());
	protected final Map<String, Cache> caches = new StrictMap<>("Caches collection");
	protected final Map<String, ResultMap> resultMaps = new StrictMap<>("Result Maps collection");
	protected final Map<String, ParameterMap> parameterMaps = new StrictMap<>("Parameter Maps collection");
	protected final Map<String, KeyGenerator> keyGenerators = new StrictMap<>("Key Generators collection");

	protected final Set<String> loadResources = new HashSet<>();
	@Getter
	protected final Map<String, XNode> sqlFragments = new StrictMap<>("XML fragments parsed from previous mappers");

	@Getter
	protected final Collection<XMLStatementBuilder> incompleteStatements = new LinkedList<>();
	@Getter
	protected final Collection<CacheRefResolver> incompleteCacheRefs = new LinkedList<>();
	@Getter
	protected final Collection<ResultMapResolver> incompleteResultMaps = new LinkedList<>();
	@Getter
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
		typeAliasRegistry.registerAlias("RAW", RawLanguageDriver.class);

		typeAliasRegistry.registerAlias("SLF4J", Slf4jImpl.class);
		typeAliasRegistry.registerAlias("COMMONS_LOGGING", JakartaCommonsLoggingImpl.class);
		typeAliasRegistry.registerAlias("LOG4J2", Log4j2Impl.class);
		typeAliasRegistry.registerAlias("JDK_LOGGING", Jdk14LoggingImpl.class);
		typeAliasRegistry.registerAlias("STDOUT_LOGGING", StdOutImpl.class);
		typeAliasRegistry.registerAlias("NO_LOGGING", NoLoggingImpl.class);

		typeAliasRegistry.registerAlias("CGLIB", CglibProxyFactory.class);
		typeAliasRegistry.registerAlias("JAVASSIST", JavassistProxyFactory.class);

		languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
		languageRegistry.register(RawLanguageDriver.class);
	}

	public void setLogImpl(Class<? extends Log> logImpl) {
		if (logImpl != null) {
			this.logImpl = logImpl;
			LogFactory.useCustomLogging(this.logImpl);
		}
	}

	public void setVfsImpl(Class<? extends VFS> vfsImpl) {
		if (vfsImpl != null) {
			this.vfsImpl = vfsImpl;
			VFS.addImplClass(this.vfsImpl);
		}
	}

	public void addLoadedResource(String resource) {
		loadResources.add(resource);
	}

	public boolean isResourceLoaded(String resource) {
		return loadResources.contains(resource);
	}

	public void setProxyFactory(ProxyFactory proxyFactory) {
		if (proxyFactory == null) {
			proxyFactory = new JavassistProxyFactory();
		}
		this.proxyFactory = proxyFactory;
	}

	public void setDefaultEnumTypeHandler(Class<? extends TypeHandler> typeHandler) {
		if (typeHandler != null) {
			getTypeHandlerRegistry().setDefaultEnumTypeHandler(typeHandler);
		}
	}

	public List<Interceptor> getInterceptors() {
		return interceptorChain.getInterceptors();
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

	public LanguageDriver getLanguageDriver(Class<? extends LanguageDriver> langClass) {
		if (langClass == null) {
			return languageRegistry.getDefaultDriver();
		}
		languageRegistry.register(langClass);
		return languageRegistry.getDriver(langClass);
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

	public Collection<String> getKeyGeneratorNames() {
		return keyGenerators.keySet();
	}

	public Collection<KeyGenerator> getKeyGenerators() {
		return keyGenerators.values();
	}

	public KeyGenerator getKeyGenerator(String id) {
		return keyGenerators.get(id);
	}

	public boolean hasKeyGenerator(String id) {
		return keyGenerators.containsKey(id);
	}

	public void addCache(Cache cache) {
		caches.put(cache.getId(), cache);
	}

	public Collection<String> getCacheNames() {
		return caches.keySet();
	}

	public Collection<Cache> getCaches() {
		return caches.values();
	}

	public Cache getCache(String id) {
		return caches.get(id);
	}

	public boolean hasCache(String id) {
		return caches.containsKey(id);
	}

	public void addResultMap(ResultMap rm) {
		resultMaps.put(rm.getId(), rm);
		checkLocallyForDiscriminatedNestedResultMaps(rm);
		checkGloballyForDiscriminatedNestedResultMaps(rm);
	}

	public Collection<String> getResultMapNames() {
		return resultMaps.keySet();
	}

	public Collection<ResultMap> getResultMaps() {
		return resultMaps.values();
	}

	public ResultMap getResultMap(String id) {
		return resultMaps.get(id);
	}

	public boolean hasResultMap(String id) {
		return resultMaps.containsKey(id);
	}

	public void addParameterMap(ParameterMap pm) {
		parameterMaps.put(pm.getId(), pm);
	}

	public Collection<String> getParameterMapNames() {
		return parameterMaps.keySet();
	}

	public Collection<ParameterMap> getParameterMaps() {
		return parameterMaps.values();
	}

	public ParameterMap getParameterMap(String id) {
		return parameterMaps.get(id);
	}

	public boolean hasParameterMap(String id) {
		return parameterMaps.containsKey(id);
	}

	public void addMappedStatement(MappedStatement ms) {
		mappedStatements.put(ms.getId(), ms);
	}

	public Collection<String> getMappedStatementNames() {
		buildAllStatements();
		return mappedStatements.keySet();
	}

	public Collection<MappedStatement> getMappedStatement() {
		buildAllStatements();
		return mappedStatements.values();
	}

	public void addIncompleteStatement(XMLStatementBuilder incompleteStatement) {
		incompleteStatements.add(incompleteStatement);
	}

	public void addIncompleteCacheRef(CacheRefResolver incompleteCacheRef) {
		incompleteCacheRefs.add(incompleteCacheRef);
	}

	public void addIncompleteResultMap(ResultMapResolver resultMapResolver) {
		incompleteResultMaps.add(resultMapResolver);
	}

	public void addIncompleteMethod(MethodResolver builder) {
		incompleteMethods.add(builder);
	}

	public MappedStatement getMappedStatement(String id) {
		return this.getMappedStatement(id, true);
	}

	public MappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
		if (validateIncompleteStatements) {
			buildAllStatements();
		}
		return mappedStatements.get(id);
	}

	public void addInterceptor(Interceptor interceptor) {
		interceptorChain.addInterceptor(interceptor);
	}

	public void addMappers(String packageName, Class<?> superType) {
		mapperRegistry.addMappers(packageName, superType);
	}

	public void addMappers(String packageName) {
		mapperRegistry.addMappers(packageName);
	}

	public <T> void addMapper(Class<T> type) {
		mapperRegistry.addMapper(type);
	}

	public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
		return mapperRegistry.getMapper(type, sqlSession);
	}

	public boolean hasMapper(Class<?> type) {
		return mapperRegistry.hasMapper(type);
	}

	public boolean hasStatement(String statementName) {
		return hasStatement(statementName, true);
	}

	public boolean hasStatement(String statementName, boolean validateIncompleteStatements) {
		if (validateIncompleteStatements) {
			buildAllStatements();
		}
		return mappedStatements.containsKey(statementName);
	}

	public void addCacheRef(String namespace, String referencedNamespace) {
		cacheRefMap.put(namespace, referencedNamespace);
	}

	protected void buildAllStatements() {
		parsePendingResultMaps();
		if (!incompleteCacheRefs.isEmpty()) {
			synchronized (incompleteCacheRefs) {
				incompleteCacheRefs.removeIf(x -> x.resolveCacheRef() != null);
			}
		}

		if (!incompleteStatements.isEmpty()) {
			synchronized (incompleteStatements) {
				incompleteStatements.removeIf(x -> {
					x.parseStatementNode();
					return true;
				});
			}
		}

		if (!incompleteMethods.isEmpty()) {
			synchronized (incompleteMethods) {
				incompleteMethods.removeIf(x -> {
					x.resolve();
					return true;
				});
			}
		}
	}

	private void parsePendingResultMaps() {
		if (incompleteResultMaps.isEmpty()) {
			return;
		}

		synchronized (incompleteResultMaps) {
			boolean resolved;
			IncompleteElementException ex = null;

			do {
				resolved = false;
				Iterator<ResultMapResolver> iterator = incompleteResultMaps.iterator();
				while (iterator.hasNext()) {
					try {
						iterator.next().resolve();
						iterator.remove();
						resolved = true;
					}
					catch (IncompleteElementException e) {
						ex = e;
					}
				}
			}
			while (resolved);

			if (!incompleteResultMaps.isEmpty() && ex != null) {
				throw ex;
			}
		}
	}

	protected String extractNamespace(String statementId) {
		int lastPeriod = statementId.lastIndexOf('.');
		return lastPeriod > 0 ? statementId.substring(0, lastPeriod) : null;
	}

	protected void checkGloballyForDiscriminatedNestedResultMaps(ResultMap rm) {
		if (rm.hasNestedResultMaps()) {
			for (Map.Entry<String, ResultMap> entry : resultMaps.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof ResultMap) {
					ResultMap entryResultMap = (ResultMap) value;
					if (!entryResultMap.hasNestedResultMaps() && entryResultMap.getDiscriminator() != null) {
						Collection<String> discriminatedResultMapNames = entryResultMap.getDiscriminator().getDiscriminatorMap().values();
						if (discriminatedResultMapNames.contains(rm.getId())) {
							entryResultMap.forceNestedResultMaps();
						}
					}
				}
			}
		}
	}

	protected void checkLocallyForDiscriminatedNestedResultMaps(ResultMap rm) {
		if (!rm.hasNestedResultMaps() && rm.getDiscriminator() != null) {
			for (Map.Entry<String, String> entry : rm.getDiscriminator().getDiscriminatorMap().entrySet()) {
				String discriminatedResultMapName = entry.getValue();
				if (hasResultMap(discriminatedResultMapName)) {
					ResultMap discriminatedResultMap = resultMaps.get(discriminatedResultMapName);
					if (discriminatedResultMap.hasNestedResultMaps()) {
						rm.forceNestedResultMaps();
						break;
					}
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
