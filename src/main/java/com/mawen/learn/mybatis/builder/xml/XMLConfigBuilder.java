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

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class XMLConfigBuilder extends BaseBuilder {

	private boolean parsed;
	private final XPathParser parser;
	private String environment;
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

	public Configuration parse() {
		if (parsed) {
			throw new BuilderException("Each XMLConfigBuilder can only be used once");
		}

		parsed = true;
		parseConfiguration(parser.evalNode("/configuration"));
		return configuration;
	}

	private void parseConfiguration(XNode root) {
		try {
			propertiesElement(root.evalNode("properties"));
			Properties settings = settingsAsProperties(root.evalNode("settings"));
			loadCustomVfs(settings);
			loadCustomLogImpl(settings);
			typeAliasesElement(root.evalNode("typeAliases"));
			pluginElement(root.evalNode("plugins"));
			objectFactoryElement(root.evalNode("objectFactory"));
			objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
			reflectorFactoryElement(root.evalNode("reflectorFactory"));

			settingsElement(settings);
			environmentsElement(root.evalNode("environments"));
			databaseIdProviderElement(root.evalNode("databaseIdProvider"));
			typeHandlerElement(root.evalNode("typeHandlers"));
			mapperElement(root.evalNode("mappers"));
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
		MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
		for (Object key : props.keySet()) {
			if (!metaConfig.hasSetter(String.valueOf(key))) {
				throw new BuilderException("The setting " + key + " is not known. Make sure you spelled it correctly (case sensitive).");
			}
		}
		return props;
	}

	private void loadCustomVfs(Properties props) {
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

	private void pluginElement(XNode parent) {
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

	private void objectFactoryElement(XNode context) {
		if (context != null) {
			String type = context.getStringAttribute("type");
			Properties properties = context.getChildrenAsProperties();
			ObjectFactory factory = (ObjectFactory) resolveClass(type).getDeclaredConstructor().newInstance();
			factory.setProperties(properties);
			configuration.setObjectFactory(factory);
		}
	}

	private void objectWrapperFactoryElement(XNode context) {
		if (context != null) {
			String type = context.getStringAttribute("type");
			ObjectWrapperFactory factory = resolveClass(type).getDeclaredConstructor().newInstance();
			configuration.setObjectWrapperFactory(factory);
		}
	}

	private void reflectorFactoryElement(XNode context) {
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
		configuration.setCallSetterOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
		configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
		configuration.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
		configuration.setLogPrefix(props.getProperty("logPrefix"));
		configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
		configuration.setShrinkWhitespacesInsql(booleanValueOf(props.getProperty("shrinkWhitespacesInSql"), false));
		configuration.setArgNameBasedConstructorAutoMapping(booleanValueOf(props.getProperty("argNameBasedConstructorAutoMapping"), false));
		configuration.setDefaultSqlProviderType(resolveClass(props.getProperty("defaultSqlProviderType")));
		configuration.setNullableOnForEach(booleanValueOf(props.getProperty("nullableOnForEach"), false));
	}

	private void environmentsElement(XNode context) {
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

	private void databaseIdProviderElement(XNode context) {
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

	private void typeHandlerElement(XNode parent) {
		if (parent != null) {
			for (XNode child : parent.getChildren()) {
				if ("package".equals(child)) {
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

	private void mapperElement(XNode parent) {
		if (parent != null) {
			for (XNode child : parent.getChildren()) {
				if ("package".equals(child.getName())) {
					String mapperPackage = child.getStringAttribute("name");
					configuration.addMappers(mapperPackage);
				}
				else {
					String resource = child.getStringAttribute("resource");
					String url = child.getStringAttribute("url");
					String mapperClass = child.getStringAttribute("class");

					if (resource != null && url == null && mapperClass == null) {
						ErrorContext.instance().resource(resource);
						try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
							XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
							mapperParser.parse();
						}
					}
					else if (resource == null && url != null && mapperClass == null) {
						ErrorContext.instance().resource(url);
						try (InputStream inputStream = Resources.getUrlAsStream(url)) {
							XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
							mapperParser.parse();
						}
					}
					else if (resource == null && url == null && mapperClass != null) {
						Class<?> mapperInterface = Resources.classForName(mapperClass);
						configuration.addMapper(mapperInterface);
					}
					else {
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
