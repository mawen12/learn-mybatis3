package com.mawen.learn.mybatis.builder.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.builder.BaseBuilder;
import com.mawen.learn.mybatis.builder.BuilderException;
import com.mawen.learn.mybatis.datasource.DataSourceFactory;
import com.mawen.learn.mybatis.executor.ErrorContext;
import com.mawen.learn.mybatis.executor.loader.ProxyFactory;
import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.io.VFS;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.mapping.DatabaseIdProvider;
import com.mawen.learn.mybatis.mapping.Environment;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.parsing.XPathParser;
import com.mawen.learn.mybatis.plugin.Interceptor;
import com.mawen.learn.mybatis.reflection.DefaultReflectorFactory;
import com.mawen.learn.mybatis.reflection.MetaClass;
import com.mawen.learn.mybatis.reflection.ReflectorFactory;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.mawen.learn.mybatis.session.AutoMappingBehavior;
import com.mawen.learn.mybatis.session.AutoMappingUnknownColumnBehavior;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ExecutorType;
import com.mawen.learn.mybatis.session.LocalCacheScope;
import com.mawen.learn.mybatis.transaction.TransactionFactory;
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.TypeAliasRegistry;

/**
 * 负责解析全局配置文件。
 * Builder 设计模式实现。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class XMLConfigBuilder extends BaseBuilder {

	/**
	 * 解析标志位，如果已经解析过，就设置为true
	 */
	private boolean parsed;
	/**
	 * 用于解析XML节点的解析器
	 */
	private final XPathParser parser;
	/**
	 * 全局配置中的environment值，代表了需要激活的环境
	 */
	private String environment;
	/**
	 * 反射工厂，用于初始化Configuration类
	 */
	private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

	public XMLConfigBuilder(Reader reader) {
		this(reader, null, null);
	}

	public XMLConfigBuilder(Reader reader, String environment) {
		this(reader, environment, null);
	}

	public XMLConfigBuilder(Reader reader, String environment, Properties props) {
		this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
	}

	public XMLConfigBuilder(InputStream inputStream) {
		this(inputStream, null, null);
	}

	public XMLConfigBuilder(InputStream inputStream, String environment) {
		this(inputStream, environment, null);
	}

	public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
		this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
	}

	private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
		super(new Configuration());
		ErrorContext.instance().resource("SQL Mapper Configuration");
		this.configuration.setVariables(props);
		this.parsed = false;
		this.environment = environment;
		this.parser = parser;
	}

	/**
	 * 解析的入口方法
	 *
	 * @return 保存全局配置的对象
	 */
	public Configuration parse() {
		// 若已经解析过了，就抛出异常
		if (parsed) {
			throw new BuilderException("Each XMLConfigBuilder can only be used once");
		}

		// 设置解析标志位
		parsed = true;

		// 解析全局配置文件中 <configuration>节点
		parseConfiguration(parser.evalNode("/configuration"));
		return configuration;
	}

	/**
	 * 解析<configuration>节点内容
	 *
	 * @param root configuration节点对象
	 */
	private void parseConfiguration(XNode root) {
		try {
			/**
			 * 解析<properties>节点
			 * 解析到 {@link com.mawen.learn.mybatis.parsing.XPathParser#variables}
			 * 		 {@link com.mawen.learn.mybatis.session.Configuration#variables}
 			 */
			propertiesElement(root.evalNode("properties"));
			/**
			 * 解析<settings>节点
			 * 具体可以配置哪些属性值：https://mybatis.org/mybatis-3/configuration.html#settings
			 * 解析为 Properties
			 */
			Properties settings = settingsAsProperties(root.evalNode("settings"));
			/**
			 * 基本没有用过该属性
			 * VFS含义是虚拟文件系统，主要是通过程序能够方便读取文件系统、FTP文件系统等系统中的文件资源。
			 * Mybatis中提供了VFS这个配置，主要是通过该配置可以加载自定义的虚拟文件系统应用程序。
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#vfsImpl}
			 */
			loadCustomVfs(settings);
			/**
			 * 指定 Mybatis 所用日志的具体实现，未指定时将自动查找。
			 *
			 * SLF4J | LOG4J | LOG4J2 | JDK_LOGGING | COMMONS_LOGGING | STDOUT_LOGGING
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#logImpl}
			 */
			loadCustomLogImpl(settings);
			/**
			 * 解析<typeAliases>节点
			 * 解析我们的别名，类型别名是Java类型的缩写，其仅于XML配置有关，其存在的目的只是为了减少完全限定类名的冗余类型。
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#typeAliasRegistry}
			 */
			//
			typeAliasesElement(root.evalNode("typeAliases"));
			/**
			 * 解析<plugins>节点
			 * 解析我们的插件，比如分页插件。
			 * 默认情况下，Mybatis允许插件拦截以下方法调用：
			 * - Executor(update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
			 * - ParameterHandler(getParameterObject, setParameters)
			 * - ResultSetHandler(handleResultSets, handleOutputParameters)
			 * - StatementHandler(prepare, parameterize, batch, update, query)
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#interceptorChain.intercetors}
			 */
			pluginsElement(root.evalNode("plugins"));
			/**
			 * 解析<objectFactory>节点
			 * 对象工程负责对象创建，设置自定的工程必须实现 {@link com.mawen.learn.mybatis.reflection.factory.ObjectFactory}。
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#objectFactory}
			 */
			objectFactoryElement(root.evalNode("objectFactory"));
			/**
			 * 解析<objectWrapperFactory>节点
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#objectWrapperFactory}
			 */
			objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
			/**
			 * 解析<reflectorFactory>节点
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#reflectorFactory}
			 */
			reflectorFactoryElement(root.evalNode("reflectorFactory"));

			settingsElement(settings);
			//
			/**
			 * 解析<environments>节点
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#environment}
			 */
			environmentsElement(root.evalNode("environments"));
			/**
			 * 解析<databaseIdProvider>节点
			 * Mybatis 支持根据不同的数据库厂商来执行不同的数据库语句。
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#databaseId}
			 */
			databaseIdProviderElement(root.evalNode("databaseIdProvider"));
			/**
			 * 解析<typeHandlers>节点
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#typeHandlerRegistry.typeHandlerMap}
			 */
			typeHandlersElement(root.evalNode("typeHandlers"));
			/**
			 * 解析<mappers>节点
			 * 解析XML映射文件
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#mapperRegistry.knownMappers}
			 */
			mappersElement(root.evalNode("mappers"));
		}
		catch (Exception e) {
			throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
		}
	}

	private void propertiesElement(XNode context) throws IOException {
		if (context != null) {
			Properties defaults = context.getChildrenAsProperties();
			String resource = context.getStringAttribute("resource");
			String url = context.getStringAttribute("url");
			if (resource != null && url != null) {
				throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference. Please specify one or the other. ");
			}
			if (resource != null) {
				defaults.putAll(Resources.getResourceAsProperties(resource));
			}
			else if (url != null) {
				defaults.putAll(Resources.getUrlAsProperties(url));
			}

			Properties vars = configuration.getVariables();
			if (vars != null) {
				defaults.putAll(vars);
			}

			parser.setVariables(defaults);
			configuration.setVariables(defaults);
		}
	}

	private Properties settingsAsProperties(XNode context) {
		if (context == null) {
			return new Properties();
		}

		Properties props = context.getChildrenAsProperties();

		// check property exists in Configuration setter method parameter
		MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
		for (Object key : props.keySet()) {
			if (!metaConfig.hasSetter(String.valueOf(key))) {
				throw new BuilderException("The setting " + key + " is not known. Make sure you spelled it correctly (case sensitive).");
			}
		}
		return props;
	}

	private void loadCustomVfs(Properties props) throws ClassNotFoundException {
		String value = props.getProperty("vfsImpl");
		if (value != null) {
			String[] clazzes = value.split(",");
			for (String clazz : clazzes) {
				if (!clazz.isEmpty()) {
					Class<? extends VFS> vfsImpl = (Class<? extends VFS>)Resources.classForName(clazz);
					configuration.setVfsImpl(vfsImpl);
				}
			}
		}
	}

	private void loadCustomLogImpl(Properties props) {
		Class<? extends Log> logImpl = resolveClass(props.getProperty("logImpl"));
		configuration.setLogImpl(logImpl);
	}

	private void typeAliasesElement(XNode parent) {
		if (parent != null) {
			for (XNode child : parent.getChildren()) {
				if ("package".equals(child.getName())) {
					String typeAliasPackage = child.getStringAttribute("name");
					configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);
				}
				else {
					String alias = child.getStringAttribute("alias");
					String type = child.getStringAttribute("type");
					try {
						Class<?> clazz = Resources.classForName(type);
						if (alias == null) {
							typeAliasRegistry.registerAlias(clazz);
						}
						else {
							typeAliasRegistry.registerAlias(alias, clazz);
						}
					}
					catch (ClassNotFoundException e) {
						throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
					}
				}
			}
		}
	}

	private void pluginsElement(XNode parent) throws Exception {
		if (parent != null) {
			for (XNode child : parent.getChildren()) {
				String interceptor = child.getStringAttribute("interceptor");
				Properties properties = child.getChildrenAsProperties();
				Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).getDeclaredConstructor().newInstance();
				interceptorInstance.setProperties(properties);
				configuration.addInterceptor(interceptorInstance);
			}
		}
	}

	private void objectFactoryElement(XNode context) throws Exception {
		if (context != null) {
			String type = context.getStringAttribute("type");
			Properties properties = context.getChildrenAsProperties();
			ObjectFactory factory = (ObjectFactory) resolveClass(type).getDeclaredConstructor().newInstance();
			factory.setProperties(properties);
			configuration.setObjectFactory(factory);
		}
	}

	private void objectWrapperFactoryElement(XNode context) throws Exception {
		if (context != null) {
			String type = context.getStringAttribute("type");
			ObjectWrapperFactory factory = (ObjectWrapperFactory) resolveClass(type).getDeclaredConstructor().newInstance();
			configuration.setObjectWrapperFactory(factory);
		}
	}

	private void reflectorFactoryElement(XNode context) throws Exception{
		if (context != null) {
			String type = context.getStringAttribute("type");
			ReflectorFactory factory = (ReflectorFactory) resolveClass(type).getDeclaredConstructor().newInstance();
			configuration.setReflectorFactory(factory);
		}
	}

	private void settingsElement(Properties props) {
		configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
		configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));
		configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
		configuration.setProxyFactory((ProxyFactory)createInstance(props.getProperty("proxyFactory")));
		configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));
		configuration.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
		configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
		configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
		configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
		configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
		configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
		configuration.setDefaultFetchSize(integerValueOf(props.getProperty("defaultFetchSize"), null));
		configuration.setDefaultResultSetType(resolveResultSetType(props.getProperty("defaultResultSetType")));
		configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
		configuration.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
		configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
		configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
		configuration.setLazyLoadTriggerMethods(stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));
		configuration.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
		configuration.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));
		configuration.setDefaultEnumTypeHandler(resolveClass(props.getProperty("defaultEnumTypeHandler")));
		configuration.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
		configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
		configuration.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
		configuration.setLogPrefix(props.getProperty("logPrefix"));
		configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
		configuration.setShrinkWhitespacesInSql(booleanValueOf(props.getProperty("shrinkWhitespacesInSql"), false));
		configuration.setArgNameBasedConstructorAutoMapping(booleanValueOf(props.getProperty("argNameBasedConstructorAutoMapping"), false));
		configuration.setDefaultSqlProviderType(resolveClass(props.getProperty("defaultSqlProviderType")));
		configuration.setNullableOnForEach(booleanValueOf(props.getProperty("nullableOnForEach"), false));
	}

	private void environmentsElement(XNode context) throws Exception {
		if (context != null) {
			if (environment == null) {
				environment = context.getStringAttribute("default");
			}

			for (XNode child : context.getChildren()) {
				String id = child.getStringAttribute("id");
				if (isSpecifiedEnvironment(id)) {
					TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
					DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
					DataSource dataSource = dsFactory.getDataSource();
					Environment environment = new Environment.Builder(id)
							.transactionFactory(txFactory)
							.dataSource(dataSource)
							.build();
					configuration.setEnvironment(environment);
					break;
				}
			}
		}
	}

	private void databaseIdProviderElement(XNode context) throws Exception{
		DatabaseIdProvider databaseIdProvider = null;
		if (context != null) {
			String type = context.getStringAttribute("type");
			if ("VENDOR".equals(type)) {
				type = "DB_VENDOR";
			}
			Properties properties = context.getChildrenAsProperties();
			databaseIdProvider = (DatabaseIdProvider) resolveClass(type).getDeclaredConstructor().newInstance();
			databaseIdProvider.setProperties(properties);
		}

		Environment environment = configuration.getEnvironment();
		if (environment != null && databaseIdProvider != null) {
			String databaseId = databaseIdProvider.getDatabaseId(environment.getDataSource());
			configuration.setDatabaseId(databaseId);
		}
	}

	private void typeHandlersElement(XNode parent) {
		if (parent != null) {
			for (XNode child : parent.getChildren()) {
				if ("package".equals(child.getName())) {
					String typeHandlerPackage = child.getStringAttribute("name");
					typeHandlerRegistry.register(typeHandlerPackage);
				}
				else {
					String javaTypeName = child.getStringAttribute("javaType");
					String jdbcTypeName = child.getStringAttribute("jdbcType");
					String handlerTypeName = child.getStringAttribute("handler");
					Class<?> javaTypeClass = resolveClass(javaTypeName);
					JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
					Class<?> typeHandlerClass = resolveClass(handlerTypeName);

					if (javaTypeClass != null) {
						if (jdbcType == null) {
							typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
						}
						else {
							typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
						}
					}
					else {
						typeHandlerRegistry.register(typeHandlerClass);
					}
				}
			}
		}
	}

	private void mappersElement(XNode parent) throws Exception {
		if (parent != null) {
			/**
			 * 获取我们mappers节点下的所有mapper节点
			 */
			for (XNode child : parent.getChildren()) {
				/**
				 * 判断mapper是不是通过批量注册的
				 * <package name=“com.mawen”></package>
				 */
				if ("package".equals(child.getName())) {
					String mapperPackage = child.getStringAttribute("name");
					configuration.addMappers(mapperPackage);
				}
				else {
					/**
					 * 从mapperClass下读取
					 * <mapper></mapper>
					 */
					String resource = child.getStringAttribute("resource");
					String url = child.getStringAttribute("url");
					String mapperClass = child.getStringAttribute("class");

					/**
					 * 仅指定resource时从classpath读取
					 */
					if (resource != null && url == null && mapperClass == null) {
						ErrorContext.instance().resource(resource);
						try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
							XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
							mapperParser.parse();
						}
					}
					/**
					 * 仅指定url时从网络地址地址加载
					 */
					else if (resource == null && url != null && mapperClass == null) {
						ErrorContext.instance().resource(url);
						try (InputStream inputStream = Resources.getUrlAsStream(url)) {
							XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
							mapperParser.parse();
						}
					}
					/**
					 * 仅指定mapperClass时从src/main/java下加载
					 */
					else if (resource == null && url == null && mapperClass != null) {
						Class<?> mapperInterface = Resources.classForName(mapperClass);
						configuration.addMapper(mapperInterface);
					}
					else {
						// 当resource、url、mapperClass均未指定时，抛出异常
						throw new BuilderException("A mapper element may only specify a url, resource or class, but not more that one.");
					}
				}
			}
		}
	}

	private TransactionFactory transactionManagerElement(XNode context) throws Exception {
		if (context != null) {
			String type = context.getStringAttribute("type");
			Properties props = context.getChildrenAsProperties();
			TransactionFactory factory = (TransactionFactory) resolveClass(type).getDeclaredConstructor().newInstance();
			factory.setProperties(props);
			return factory;
		}
		throw new BuilderException("Environment declaration requires a TransactionFactory.");
	}

	private DataSourceFactory dataSourceElement(XNode context) throws Exception {
		if (context != null) {
			String type = context.getStringAttribute("type");
			Properties props = context.getChildrenAsProperties();
			DataSourceFactory factory = (DataSourceFactory) resolveClass(type).getDeclaredConstructor().newInstance();
			factory.setProperties(props);
			return factory;
		}
		throw new BuilderException("Environment declaration requires a DataSourceFactory.");
	}

	private boolean isSpecifiedEnvironment(String id) {
		if (environment == null) {
			throw new BuilderException("No environment specified.");
		}
		if (id == null) {
			throw new BuilderException("Environment requires an id attribute.");
		}
		return environment.equals(id);
	}
}
