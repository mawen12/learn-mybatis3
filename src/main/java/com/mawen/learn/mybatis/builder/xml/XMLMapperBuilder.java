package com.mawen.learn.mybatis.builder.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.mawen.learn.mybatis.builder.BaseBuilder;
import com.mawen.learn.mybatis.builder.BuilderException;
import com.mawen.learn.mybatis.builder.CacheRefResolver;
import com.mawen.learn.mybatis.builder.IncompleteElementException;
import com.mawen.learn.mybatis.builder.MapperBuilderAssistant;
import com.mawen.learn.mybatis.builder.ResultMapResolver;
import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.executor.ErrorContext;
import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.mapping.Discriminator;
import com.mawen.learn.mybatis.mapping.ParameterMapping;
import com.mawen.learn.mybatis.mapping.ParameterMode;
import com.mawen.learn.mybatis.mapping.ResultFlag;
import com.mawen.learn.mybatis.mapping.ResultMap;
import com.mawen.learn.mybatis.mapping.ResultMapping;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.parsing.XPathParser;
import com.mawen.learn.mybatis.reflection.MetaClass;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.TypeHandler;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class XMLMapperBuilder extends BaseBuilder {

	private final XPathParser parser;
	private final MapperBuilderAssistant builderAssistant;
	private final Map<String, XNode> sqlFragments;
	private final String resource;

	public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
		this(reader, configuration, resource, sqlFragments);
		this.builderAssistant.setCurrentNamespace(namespace);
	}

	public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
		this(new XPathParser(reader, true, configuration.getVariables(), new XMLMapperEntityResolver()), configuration, resource, sqlFragments);
	}

	public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
		this(inputStream, configuration, resource, sqlFragments);
		this.builderAssistant.setCurrentNamespace(namespace);
	}

	public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
		this(new XPathParser(inputStream, true, configuration.getVariables(), new XMLMapperEntityResolver()), configuration, resource, sqlFragments);
	}

	public XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
		super(configuration);
		this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
		this.parser = parser;
		this.sqlFragments = sqlFragments;
		this.resource = resource;
	}

	public void parse() {
		if (!configuration.isResourceLoaded(resource)) {
			/**
			 * 真正解析我们的<mapper namespace=""></mapper>
			 * 解析的数据内容如下：https://mybatis.org/mybatis-3/sqlmap-xml.html
			 */
			configurationElement(parser.evalNode("/mapper"));
			/**
			 * 把资源保存到Configuration中
			 */
			configuration.addLoadedResource(resource);
			bindMapperForNamespace();
		}

		parsePendingResultMaps();
		parsePendingCacheRefs();
		parsePendingStatements();
	}

	private XNode getSqlFragment(String refid) {
		return sqlFragments.get(refid);
	}

	private void configurationElement(XNode context) {
		try {
			/**
			 * 解析namespace属性
			 */
			String namespace = context.getStringAttribute("namespace");
			if (namespace == null || namespace.isEmpty()) {
				throw new BuilderException("Mapper's namespace cannot be empty");
			}
			/**
			 * 保存当前的namespace，并且判断接口完全类型==namespace
			 */
			builderAssistant.setCurrentNamespace(namespace);
			/**
			 * 解析缓存引用
			 * 说明当前的缓存引用和namespace完全一致
			 * <cache-ref namespace="com.someone.application.data.SomeMapper"/>
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#cacheRefMap}
			 * 异常下（引用缓存未使用缓存） {@link com.mawen.learn.mybatis.session.Configuration#incompleteCacheRefs}
			 */
			cacheRefElement(context.evalNode("cache-ref"));
			/**
			 * 解析缓存
			 * 缓存规则：
			 * - 当前映射文件中查询语句的结果将会被缓存
			 * - 当前映射文件中新增、修改、删除会刷新缓存
			 * - 缓存将使用LRU（最近最少使用）算法来驱逐
			 * - 缓存不会按照任何基于时间的计划进行刷新（即无刷新间隔）
			 * - 缓存最多存储1024个列表或对象（无论查询方法返回什么）
			 * - 缓存被是为读写缓存，这意味着检索到的对象不共享，并且可以由调用者安全地修改，而不会干扰其他调用者或线程的潜在修改
			 * <cache
			 *   eviction="FIFO"
			 *   flushInterval="60000"
			 *   size="512"
			 *   readOnly="true"/>
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#caches}
			 * 		 {@link com.mawen.learn.mybatis.builder.MapperBuilderAssistant#currentCache}
			 */
			cacheElement(context.evalNode("cache"));
			/**
			 * 解析parameterMap节点（该节点mybatis3.5貌似不推荐使用了）
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#parameterMaps}
			 */
			parameterMapElement(context.evalNodes("/mapper/parameterMap"));
			/**
			 * 解析resultMap节点
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#resultMaps}
			 */
			resultMapElements(context.evalNodes("/mapper/resultMap"));
			/**
			 * 解析sql节点
			 * 解析到 {@link sqlFragments}
			 */
			sqlElement(context.evalNodes("/mapper/sql"));
			/**
			 * 解析增删改查节点
			 * 解析到 {@link com.mawen.learn.mybatis.session.Configuration#mappedStatements}
			 */
			buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
		}
		catch (Exception e) {
			throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "', Cause: " + e, e);
		}
	}

	private void buildStatementFromContext(List<XNode> list) {
		if (configuration.getDatabaseId() != null) {
			buildStatementFromContext(list, configuration.getDatabaseId());
		}
		buildStatementFromContext(list, null);
	}

	private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
		for (XNode context : list) {
			final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
			try {
				statementParser.parseStatementNode();
			}
			catch (IncompleteElementException e) {
				configuration.addIncompleteStatement(statementParser);
			}
		}
	}

	private void parsePendingResultMaps() {
		Collection<ResultMapResolver> incompleteResultMaps = configuration.getIncompleteResultMaps();
		synchronized (incompleteResultMaps) {
			Iterator<ResultMapResolver> iter = incompleteResultMaps.iterator();
			while (iter.hasNext()) {
				try {
					iter.next().resolve();
					iter.remove();
				}
				catch (IncompleteElementException ignored) {
				}
			}
		}
	}

	private void parsePendingCacheRefs() {
		Collection<CacheRefResolver> incompleteResultMaps = configuration.getIncompleteCacheRefs();
		synchronized (incompleteResultMaps) {
			Iterator<CacheRefResolver> iter = incompleteResultMaps.iterator();
			while (iter.hasNext()) {
				try {
					iter.next().resolveCacheRef();
					iter.remove();
				}
				catch (IncompleteElementException ignored) {

				}
			}
		}
	}

	private void parsePendingStatements() {
		Collection<XMLStatementBuilder> incompleteStatements = configuration.getIncompleteStatements();
		synchronized (incompleteStatements) {
			Iterator<XMLStatementBuilder> iter = incompleteStatements.iterator();
			while (iter.hasNext()) {
				try {
					iter.next().parseStatementNode();
					iter.remove();
				}
				catch (IncompleteElementException ignored) {
				}
			}
		}
	}

	private void cacheRefElement(XNode context) {
		if (context != null) {
			configuration.addCacheRef(builderAssistant.getCurrentNamespace(), context.getStringAttribute("namespace"));
			CacheRefResolver cacheRefResolver = new CacheRefResolver(builderAssistant, context.getStringAttribute("namespace"));
			try {
				cacheRefResolver.resolveCacheRef();
			}
			catch (IncompleteElementException e) {
				configuration.addIncompleteCacheRef(cacheRefResolver);
			}
		}
	}

	private void cacheElement(XNode context) {
		if (context != null) {
			/**
			 * 读取缓存类型属性，默认是PERPETUAL，该值会被Configuration预注册到TypeAliasRegistry中(com.mawen.learn.mybatis.session.Configuration#Configuration())，对应 {@link com.mawen.learn.mybatis.cache.impl.PerpetualCache}
			 */
			String type = context.getStringAttribute("type", "PERPETUAL");
			/**
			 * 从别名中查找预注册的缓存类型
			 */
			Class<? extends Cache> typeClass = resolveAlias(type);
			/**
			 * 读取缓存过期策略，默认是LRU，该值会被Configuration预注册到TypeAliasRegistry中(com.mawen.learn.mybatis.session.Configuration#Configuration())，对应{@link sun.misc.LRUCache}
			 */
			String eviction = context.getStringAttribute("eviction", "LRU");
			/**
			 * 从别名中查找预注册的驱逐策略
			 */
			Class<? extends Cache> evictionClass = resolveAlias(eviction);
			/**
			 * flushInterval(刷新间隔)，属性可以被设置为任意正整数，设置的值应该是一个以毫秒为单位的合理时间
			 */
			Long flushInterval = context.getLongAttribute("flushInterval");
			/**
			 * size(引用数目)，属性可以被设置为任意正整数，要注意欲缓存对象的大小和运行环境中可用的内存资源，默认值为1024
			 */
			Integer size = context.getIntAttribute("size");
			/**
			 * readOnly(只读)，属性可以被设置为true和false。只读的缓存会给所有调用者返回缓存对象的相同实例。
			 */
			boolean readWrite = !context.getBooleanAttribute("readOnly", false);
			/**
			 * blocking(阻塞操作)，属性可以被设置为true和false，操作缓存时是否阻塞其他线程，默认值为false
			 */
			boolean blocking = context.getBooleanAttribute("blocking", false);
			Properties props = context.getChildrenAsProperties();
			// 把缓存节点加入到Configuration中
			builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
		}
	}

	private void parameterMapElement(List<XNode> list) {
		for (XNode parameterMapNode : list) {
			String id = parameterMapNode.getStringAttribute("id");
			String type = parameterMapNode.getStringAttribute("type");
			Class<?> parameterClass = resolveClass(type);
			List<XNode> parameterNodes = parameterMapNode.evalNodes("parameter");
			List<ParameterMapping> parameterMappings = new ArrayList<>();
			for (XNode parameterNode : parameterNodes) {
				String property = parameterNode.getStringAttribute("property");
				String javaType = parameterNode.getStringAttribute("javaType");
				String jdbcType = parameterNode.getStringAttribute("jdbcType");
				String resultMap = parameterNode.getStringAttribute("resultMap");
				String mode = parameterNode.getStringAttribute("mode");
				String typeHandler = parameterNode.getStringAttribute("typeHandler");
				Integer numericScale = parameterNode.getIntAttribute("numericScale");

				ParameterMode modeEnum = resolveParameterMode(mode);
				Class<?> javaTypeClass = resolveClass(javaType);
				JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
				Class<? extends TypeHandler<?>> typeHandlerClass = resolveClass(typeHandler);

				ParameterMapping parameterMapping = builderAssistant.buildParameterMapping(parameterClass, property, javaTypeClass, jdbcTypeEnum,
						resultMap, modeEnum, typeHandlerClass, numericScale);
				parameterMappings.add(parameterMapping);
			}

			builderAssistant.addParameterMap(id, parameterClass, parameterMappings);
		}
	}

	private void resultMapElements(List<XNode> list) {
		for (XNode resultMapNode : list) {
			try {
				resultMapElement(resultMapNode);
			}
			catch (IncompleteElementException ignored) {}
		}
	}

	private ResultMap resultMapElement(XNode resultMapNode) {
		return resultMapElement(resultMapNode, Collections.emptyList(), null);
	}

	private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings, Class<?> enclosingType) {
		ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
		String type = resultMapNode.getStringAttribute("type",
				resultMapNode.getStringAttribute("ofType",
						resultMapNode.getStringAttribute("resultType",
								resultMapNode.getStringAttribute("javaType"))));
		Class<?> typeClass = resolveClass(type);
		if (typeClass == null) {
			typeClass = inheritEnclosingType(resultMapNode, enclosingType);
		}

		Discriminator discriminator = null;
		List<ResultMapping> resultMappings = new ArrayList<>(additionalResultMappings);
		List<XNode> resultChildren = resultMapNode.getChildren();
		for (XNode resultChild : resultChildren) {
			if ("constructor".equals(resultChild.getName())) {
				processConstructorElement(resultChild, typeClass, resultMappings);
			}
			else if ("discriminator".equals(resultChild.getName())) {
				discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
			}
			else {
				List<ResultFlag> flags = new ArrayList<>();
				if ("id".equals(resultChild.getName())) {
					flags.add(ResultFlag.ID);
				}
				resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
			}
		}

		String id = resultMapNode.getStringAttribute("id", resultMapNode::getValueBasedIdentifier);
		String extend = resultMapNode.getStringAttribute("extends");
		Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
		ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);
		try {
			return resultMapResolver.resolve();
		}
		catch (IncompleteElementException e) {
			configuration.addIncompleteResultMap(resultMapResolver);
			throw e;
		}
	}

	protected Class<?> inheritEnclosingType(XNode resultMapNode, Class<?> enclosingType) {
		if ("association".equals(resultMapNode.getName()) && resultMapNode.getStringAttribute("resultMap") == null) {
			String property = resultMapNode.getStringAttribute("property");
			if (property != null && enclosingType != null) {
				MetaClass metaResultType = MetaClass.forClass(enclosingType, configuration.getReflectorFactory());
				return metaResultType.getSetterType(property);
			}
		}
		else if ("case".equals(resultMapNode.getName()) && resultMapNode.getStringAttribute("resultMap") == null) {
			return enclosingType;
		}

		return null;
	}

	private void processConstructorElement(XNode constructorNode, Class<?> resultType, List<ResultMapping> resultMappings) {
		List<XNode> argChildren = constructorNode.getChildren();
		for (XNode argChild : argChildren) {
			List<ResultFlag> flags = new ArrayList<>();
			flags.add(ResultFlag.CONSTRUCTOR);
			if ("idArg".equals(argChild.getName())) {
				flags.add(ResultFlag.ID);
			}
			resultMappings.add(buildResultMappingFromContext(argChild, resultType, flags));
		}
	}

	private Discriminator processDiscriminatorElement(XNode context, Class<?> resultType, List<ResultMapping> resultMappings) {
		String column = context.getStringAttribute("column");
		String javaType = context.getStringAttribute("javaType");
		String jdbcType = context.getStringAttribute("jdbcType");
		String typeHandler = context.getStringAttribute("typeHandler");
		Class<?> javaTypeClass = resolveClass(javaType);
		Class<? extends TypeHandler<?>> typeHandlerClass = resolveClass(typeHandler);
		JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);

		Map<String, String> discriminatorMap = new HashMap<>();
		for (XNode caseChild : context.getChildren()) {
			String value = caseChild.getStringAttribute("value");
			String resultMap = caseChild.getStringAttribute("resultMap", () -> processNestedResultMappings(caseChild, resultMappings, resultType));
			discriminatorMap.put(value, resultMap);
		}
		return builderAssistant.buildDiscriminator(resultType, column, javaTypeClass, jdbcTypeEnum, typeHandlerClass, discriminatorMap);
	}

	private void sqlElement(List<XNode> list) {
		if (configuration.getDatabaseId() != null) {
			sqlElement(list, configuration.getDatabaseId());
		}
		sqlElement(list, null);
	}

	private void sqlElement(List<XNode> list, String requiredDatabaseId) {
		for (XNode context : list) {
			String databaseId = context.getStringAttribute("databaseId");
			String id = context.getStringAttribute("id");
			id = builderAssistant.applyCurrentNamespace(id, false);
			if (databaseIdMatchesCurrent(id, databaseId, requiredDatabaseId)) {
				sqlFragments.put(id, context);
			}
		}
	}

	private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
		if (requiredDatabaseId != null) {
			return requiredDatabaseId.equals(databaseId);
		}

		if (databaseId != null) {
			return false;
		}

		if (!this.sqlFragments.containsKey(id)) {
			return true;
		}

		XNode context = this.sqlFragments.get(id);
		return context.getStringAttribute("databaseId") == null;
	}

	private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) {
		String property;

		if (flags.contains(ResultFlag.CONSTRUCTOR)) {
			property = context.getStringAttribute("name");
		}
		else {
			property = context.getStringAttribute("property");
		}

		String column = context.getStringAttribute("column");
		String javaType = context.getStringAttribute("javaType");
		String jdbcType = context.getStringAttribute("jdbcType");
		String nestedSelect = context.getStringAttribute("select");
		String nestedResultMap = context.getStringAttribute("resultMap", () -> processNestedResultMappings(context, Collections.emptyList(), resultType));
		String notNulColumn = context.getStringAttribute("notNullColumn");
		String columnPrefix = context.getStringAttribute("columnPrefix");
		String typeHandler = context.getStringAttribute("typeHandler");
		String resultSet = context.getStringAttribute("resultSet");
		String foreignColumn = context.getStringAttribute("foreignColumn");
		boolean lazy = "lazy".equals(context.getStringAttribute("fetchType", configuration.isLazyLoadingEnabled() ? "lazy" : "eager"));
		Class<?> javaTypeClass = resolveClass(javaType);
		Class<? extends TypeHandler<?>> typeHandlerClass = resolveClass(typeHandler);
		JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
		return builderAssistant.buildResultMapping(resultType, property, column, javaTypeClass, jdbcTypeEnum,
				nestedSelect, nestedResultMap, notNulColumn, columnPrefix, typeHandlerClass, flags, resultSet, foreignColumn, lazy);
	}

	private String processNestedResultMappings(XNode context, List<ResultMapping> resultMappings, Class<?> enclosingType) {
		if (Arrays.asList("association", "collection", "case").contains(context.getName()) && context.getStringAttribute("select") == null) {
			validateCollection(context, enclosingType);
			ResultMap resultMap = resultMapElement(context, resultMappings, enclosingType);
			return resultMap.getId();
		}
		return null;
	}

	protected void validateCollection(XNode context, Class<?> enclosingType) {
		if ("collection".equals(context.getName())
		    && context.getStringAttribute("resultMap") == null
		    && context.getStringAttribute("javaType") == null) {
			MetaClass metaResultType = MetaClass.forClass(enclosingType, configuration.getReflectorFactory());
			String property = context.getStringAttribute("property");
			if (!metaResultType.hasSetter(property)) {
				throw new BuilderException("Ambiguous collection type for property '" + property + "'. You must specify 'javaType' or 'resultMap'.");
			}
		}
	}

	private void bindMapperForNamespace() {
		String namespace = builderAssistant.getCurrentNamespace();
		if (namespace == null) {
			Class<?> boundType = null;
			try {
				boundType = Resources.classForName(namespace);
			}
			catch (ClassNotFoundException ignored) {

			}

			if (boundType != null && !configuration.hasMapper(boundType)) {
				configuration.addLoadedResource("namespace:" + namespace);
				configuration.addMapper(boundType);
			}
		}
	}
}
