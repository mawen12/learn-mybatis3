package com.mawen.learn.mybatis.executor.resultset;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mawen.learn.mybatis.annotations.AutomapConstructor;
import com.mawen.learn.mybatis.annotations.Param;
import com.mawen.learn.mybatis.binding.MapperMethod;
import com.mawen.learn.mybatis.cache.CacheKey;
import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.cursor.defaults.DefaultCursor;
import com.mawen.learn.mybatis.executor.ErrorContext;
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.executor.loader.ResultLoader;
import com.mawen.learn.mybatis.executor.loader.ResultLoaderMap;
import com.mawen.learn.mybatis.executor.parameter.ParameterHandler;
import com.mawen.learn.mybatis.executor.result.DefaultResultContext;
import com.mawen.learn.mybatis.executor.result.DefaultResultHandler;
import com.mawen.learn.mybatis.executor.result.ResultMapException;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.Discriminator;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.ParameterMapping;
import com.mawen.learn.mybatis.mapping.ParameterMode;
import com.mawen.learn.mybatis.mapping.ResultMap;
import com.mawen.learn.mybatis.mapping.ResultMapping;
import com.mawen.learn.mybatis.reflection.MetaClass;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.ReflectorFactory;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.session.AutoMappingBehavior;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ResultContext;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;
import com.mawen.learn.mybatis.util.MapUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/13
 */
public class DefaultResultSetHandler implements ResultSetHandler {

	private static final Object DEFERRED = new Object();

	private final Executor executor;
	private final Configuration configuration;
	private final MappedStatement mappedStatement;
	private final RowBounds rowBounds;
	private final ParameterHandler parameterHandler;
	private final ResultHandler<?> resultHandler;
	private final BoundSql boundSql;
	private final TypeHandlerRegistry typeHandlerRegistry;
	private final ObjectFactory objectFactory;
	private final ReflectorFactory reflectorFactory;

	private final Map<CacheKey, Object> nestedResultObjects = new HashMap<>();
	private final Map<String, Object> ancestorObjects = new HashMap<>();
	private Object previousRowValue;

	private final Map<String, ResultMapping> nextResultMaps = new HashMap<>();
	private final Map<CacheKey, List<PendingRelation>> pendingRelations = new HashMap<>();

	private final Map<String, List<UnMappedColumnAutoMapping>> autoMappingsCache = new HashMap<>();
	private final Map<String, List<String>> constructorAutoMappingColumns = new HashMap<>();

	private boolean useConstructorMappings;

	public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler<?> resultHandler, BoundSql boundSql, RowBounds rowBounds) {
		this.executor = executor;
		this.configuration = mappedStatement.getConfiguration();
		this.mappedStatement = mappedStatement;
		this.rowBounds = rowBounds;
		this.parameterHandler = parameterHandler;
		this.boundSql = boundSql;
		this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
		this.objectFactory = configuration.getObjectFactory();
		this.reflectorFactory = configuration.getReflectorFactory();
		this.resultHandler = resultHandler;
	}

	@Override
	public List<Object> handleResultSets(Statement statement) throws SQLException {
		ErrorContext.instance().activity("handing results").object(mappedStatement.getId());

		List<Object> multipleResults = new ArrayList<>();
		int resultSetCount = 0;
		ResultSetWrapper rsw = getFirstResultSet(statement);

		List<ResultMap> resultMaps = mappedStatement.getResultMaps();
		int resultMapCount = resultMaps.size();
		validateResultMapsCount(rsw, resultMapCount);

		while (rsw != null && resultMapCount > resultSetCount) {
			ResultMap resultMap = resultMaps.get(resultSetCount);
			handleResultSet(rsw, resultMap, multipleResults, null);
			rsw = getNextResultSet(statement);
			cleanupAfterHandlingResultSet();
			resultSetCount++;
		}

		String[] resultSets = mappedStatement.getResultSets();
		if (resultSets != null) {
			while (rsw != null && resultSetCount < resultSets.length) {
				ResultMapping parentMapping = nextResultMaps.get(resultSets[resultSetCount]);
				if (parentMapping != null) {
					String nestedResultMapId = parentMapping.getNestedResultMapId();
					ResultMap resultMap = configuration.getResultMap(nestedResultMapId);
					handleResultSet(rsw, resultMap, null, parentMapping);
				}
				rsw = getNextResultSet(statement);
				cleanupAfterHandlingResultSet();
				resultSetCount++;
			}
		}

		return collapseSingleResultList(multipleResults);
	}

	@Override
	public <E> Cursor<E> handleCursorResultSets(Statement statement) throws SQLException {
		ErrorContext.instance().activity("handling cursor results").object(mappedStatement.getId());

		ResultSetWrapper rsw = getFirstResultSet(statement);

		List<ResultMap> resultMaps = mappedStatement.getResultMaps();

		int resultMapCount = resultMaps.size();
		validateResultMapsCount(rsw, resultMapCount);
		if (resultMapCount != 1) {
			throw new ExecutorException("Cursor results cannot be mapped to multiple resultMaps");
		}

		ResultMap resultMap = resultMaps.get(0);
		return new DefaultCursor<>(this, resultMap, rsw, rowBounds);
	}

	@Override
	public void handleOutputParameters(CallableStatement cs) throws SQLException {
		Object parameterObject = parameterHandler.getParameterObject();
		MetaObject metaParam = configuration.newMetaObject(parameterObject);
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

		for (int i = 0; i < parameterMappings.size(); i++) {
			ParameterMapping parameterMapping = parameterMappings.get(i);
			if (parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT) {
				if (ResultSet.class.equals(parameterMapping.getJavaType())) {
					handleRefCursorOutputParameter((ResultSet) cs.getObject(i + 1), parameterMapping, metaParam);
				}
				else {
					TypeHandler<?> typeHandler = parameterMapping.getTypeHandler();
					metaParam.setValue(parameterMapping.getProperty(), typeHandler.getResult(cs, i + 1));
				}
			}
		}
	}

	private void handleRefCursorOutputParameter(ResultSet rs, ParameterMapping parameterMapping, MetaObject metaParam) throws SQLException {
		if (rs == null) {
			return;
		}

		try {
			String resultMapId = parameterMapping.getResultMapId();
			ResultMap resultMap = configuration.getResultMap(resultMapId);
			ResultSetWrapper rsw = new ResultSetWrapper(rs, configuration);

			if (this.resultHandler == null) {
				DefaultResultHandler resultHandler = new DefaultResultHandler(objectFactory);
				handleRowValues(rsw, resultMap, resultHandler, new RowBounds(), null);
				metaParam.setValue(parameterMapping.getProperty(), resultHandler.getResultList());
			}
			else {
				handleRowValues(rsw, resultMap, resultHandler, new RowBounds(), null);
			}
		}
		finally {
			closeResultSet(rs);
		}
	}

	private ResultSetWrapper getFirstResultSet(Statement stmt) throws SQLException {
		ResultSet rs = stmt.getResultSet();
		while (rs == null) {
			if (stmt.getMoreResults()) {
				rs = stmt.getResultSet();
			}
			else {
				if (stmt.getUpdateCount() == -1) {
					break;
				}
			}
		}
		return rs != null ? new ResultSetWrapper(rs, configuration) : null;
	}

	private ResultSetWrapper getNextResultSet(Statement stmt) {
		try {
			if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
				if (!(!stmt.getMoreResults() && stmt.getUpdateCount() == -1)) {
					ResultSet rs = stmt.getResultSet();
					if (rs == null) {
						return getNextResultSet(stmt);
					}
					else {
						return new ResultSetWrapper(rs, configuration);
					}
				}
			}
		}
		catch (Exception ignored) {

		}
		return null;
	}

	private void closeResultSet(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		}
		catch (SQLException ignored) {

		}
	}

	private void cleanupAfterHandlingResultSet() {
		nestedResultObjects.clear();
	}

	private void validateResultMapsCount(ResultSetWrapper rsw, int resultMapCount) {
		if (rsw != null && resultMapCount < 1) {
			throw new ExecutorException("A query was run and no Result Maps were found for the Mapped Statement '" + mappedStatement.getId()
			                            + "'.  It's likely that neither a Result Type nor a Result Map was specified.");
		}
	}

	private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
		try {
			if (parentMapping != null) {
				handleRowValues(rsw, resultMap, null, RowBounds.DEFAULT, parentMapping);
			}
			else {
				if (resultHandler == null) {
					DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
					handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null);
					multipleResults.add(defaultResultHandler.getResultList());
				}
				else {
					handleRowValues(rsw, resultMap, resultHandler, rowBounds, null);
				}
			}
		}
		finally {
			closeResultSet(rsw.getResultSet());
		}
	}

	private List<Object> collapseSingleResultList(List<Object> multipleResults) {
		return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
	}

	public void handleRowValues(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
		if (resultMap.hasNestedResultMaps()) {
			ensureNoRowBounds();
			checkResultHandler();
			handleRowValuesForNestedResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
		}
		else {
			handleRowValuesForSimpleResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
		}
	}

	private void ensureNoRowBounds() {
		if (configuration.isSafeRowBoundsEnabled() && rowBounds != null && (rowBounds.getLimit() < RowBounds.NO_ROW_LIMIT || rowBounds.getOffset() > RowBounds.NO_ROW_OFFSET)) {
			throw new ExecutorException("Mapped Statements with nested result mappings cannot be safely constrained by  RowBounds. "
			                            + "Use safeResultHandlerEnabled=false setting to bypass this check.");
		}
	}

	protected void checkResultHandler() {
		if (resultHandler == null && configuration.isSafeResultHandlerEnabled() && !mappedStatement.isResultOrdered()) {
			throw new ExecutorException("Mapped Statements with nested result mappings cannot be safely used with a custom ResultHandler. "
			                            + "Use safeResultHandlerEnabled=false setting to bypass this check "
			                            + "or ensure your statement returns ordered data and set resultOrdered=true on it.");
		}
	}

	private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
		DefaultResultContext<Object> resultContext = new DefaultResultContext<>();
		ResultSet resultSet = rsw.getResultSet();
		skipRows(resultSet, rowBounds);

		while (shouldProcessMoreRows(resultContext, rowBounds) && !resultSet.isClosed() && resultSet.next()) {
			ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(resultSet, resultMap, null);
			Object rowValue = getRowValue(rsw, discriminatedResultMap, null);
			storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet);
		}
	}

	private void storeObject(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext, Object rowValue, ResultMapping parentMapping, ResultSet rs) throws SQLException {
		if (parentMapping != null) {
			linkToParents(rs, parentMapping, rowValue);
		}
		else {
			callResultHandler(resultHandler, resultContext, rowValue);
		}
	}

	private void callResultHandler(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext, Object rowValue) {
		resultContext.nextResultObject(rowValue);
		((ResultHandler<Object>) resultHandler).handleResult(resultContext);
	}

	private boolean shouldProcessMoreRows(ResultContext<?> context, RowBounds rowBounds) {
		return !context.isStopped() && context.getResultCount() < rowBounds.getLimit();
	}

	private void skipRows(ResultSet rs, RowBounds rowBounds) throws SQLException {
		if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
			if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET) {
				rs.absolute(rowBounds.getOffset());
			}
		}
		else {
			for (int i = 0; i < rowBounds.getOffset(); i++) {
				if (!rs.next()) {
					break;
				}
			}
		}
	}

	private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
		ResultLoaderMap lazyLoader = new ResultLoaderMap();
		Object rowValue = createResultObject(rsw, resultMap, lazyLoader, columnPrefix);
		if (rowValue != null && !hasTypeHandlersForResultObject(rsw, resultMap.getType())) {
			MetaObject metaObject = configuration.newMetaObject(rowValue);
			boolean foundValues = this.useConstructorMappings;
			if (shouldApplyAutomaticMappings(resultMap, false)) {
				foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, columnPrefix) || foundValues;
			}
			foundValues = applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, columnPrefix) || foundValues;
			foundValues = lazyLoader.size() > 0 || foundValues;
			rowValue = foundValues || configuration.isReturnInstanceForEmptyRow() ? rowValue : null;
		}
		return rowValue;
	}

	private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap, CacheKey combineKey, String columnPrefix, Object partialObject) throws SQLException {
		String resultMapId = resultMap.getId();
		Object rowValue = partialObject;
		if (rowValue != null) {
			MetaObject metaObject = configuration.newMetaObject(rowValue);
			putAncestor(rowValue, resultMapId);
			applyNestedResultMappings(rsw, resultMap, metaObject, columnPrefix, combineKey, false);
			ancestorObjects.remove(resultMap);
		}
		else {
			ResultLoaderMap lazyLoader = new ResultLoaderMap();
			rowValue = createResultObject(rsw, resultMap, lazyLoader, columnPrefix);
			if (rowValue != null && !hasTypeHandlersForResultObject(rsw, resultMap.getType())) {
				MetaObject metaObject = configuration.newMetaObject(rowValue);
				boolean foundValues = this.useConstructorMappings;
				if (shouldApplyAutomaticMappings(resultMap, true)) {
					foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, columnPrefix) || foundValues;
				}
				foundValues = applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, columnPrefix) || foundValues;
				putAncestor(rowValue, resultMapId);
				foundValues = applyNestedResultMappings(rsw, resultMap, metaObject, columnPrefix, combineKey, true) || foundValues;
				ancestorObjects.remove(resultMapId);
				foundValues = lazyLoader.size() > 0 || foundValues;
				rowValue = foundValues || configuration.isReturnInstanceForEmptyRow() ? rowValue : null;
			}

			if (combineKey != CacheKey.NULL_CACHE_KEY) {
				nestedResultObjects.put(combineKey, rowValue);
			}
		}

		return rowValue;
	}

	private void putAncestor(Object resultObject, String resultMapId) {
		ancestorObjects.put(resultMapId, resultObject);
	}

	private boolean shouldApplyAutomaticMappings(ResultMap resultMap, boolean isNested) {
		if (resultMap.getAutoMapping() != null) {
			return resultMap.getAutoMapping();
		}
		else {
			if (isNested) {
				return AutoMappingBehavior.FULL == configuration.getAutoMappingBehavior();
			}
			else {
				return AutoMappingBehavior.NONE == configuration.getAutoMappingBehavior();
			}
		}
	}

	private boolean applyPropertyMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
		List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix);
		boolean foundValues = false;
		List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();

		for (ResultMapping propertyMapping : propertyMappings) {
			String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
			if (propertyMapping.getNestedResultMapId() != null) {
				column = null;
			}

			if (propertyMapping.isCompositeResult() || (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) || propertyMapping.getResultSet() != null) {
				Object value = getPropertyMappingValue(rsw.getResultSet(), metaObject, propertyMapping, lazyLoader, columnPrefix);
				String property = propertyMapping.getProperty();
				if (property == null) {
					continue;
				}
				else if (value == DEFERRED) {
					foundValues = true;
					continue;
				}

				if (value != null) {
					foundValues = true;
				}
				if (value != null || (configuration.isCallSettersOnNulls() && !metaObject.getSetterType(property).isPrimitive())) {
					metaObject.setValue(property, value);
				}
			}
		}
		return foundValues;
	}

	private Object getPropertyMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
		if (propertyMapping.getNestedQueryId() != null) {
			return getNestedQueryMappingValue(rs, metaResultObject, propertyMapping, lazyLoader, columnPrefix);
		}
		else if (propertyMapping.getResultSet() != null) {
			addPendingChildRelation(rs, metaResultObject, propertyMapping);
			return DEFERRED;
		}
		else {
			TypeHandler<?> typeHandler = propertyMapping.getTypeHandler();
			String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
			return typeHandler.getResult(rs, column);
		}
	}

	private List<UnMappedColumnAutoMapping> createAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) {
		String mapKey = resultMap.getId() + ":" + columnPrefix;
		List<UnMappedColumnAutoMapping> autoMapping = autoMappingsCache.get(mapKey);
		if (autoMapping == null) {
			autoMapping = new ArrayList<>();
			List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
			List<String> mappedInConstructorAutoMapping = constructorAutoMappingColumns.remove(mapKey);

			if (mappedInConstructorAutoMapping != null) {
				unmappedColumnNames.removeAll(mappedInConstructorAutoMapping);
			}

			for (String columnName : unmappedColumnNames) {
				String propertyName = columnName;
				if (columnPrefix != null && !columnPrefix.isEmpty()) {
					if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
						propertyName = columnName.substring(columnPrefix.length());
					}
					else {
						continue;
					}
				}

				final String property = metaObject.findProperty(propertyName, configuration.isMapUnderscoreToCamelCase());
				if (property != null && metaObject.hasSetter(property)) {
					if (resultMap.getMappedProperties().contains(property)) {
						continue;
					}

					Class<?> properType = metaObject.getSetterType(property);
					if (typeHandlerRegistry.hasTypeHandler(properType, rsw.getJdbcType(columnName))) {
						TypeHandler<?> typeHandler = rsw.getTypeHandler(properType, columnName);
						autoMapping.add(new UnMappedColumnAutoMapping(columnName, property, typeHandler, properType.isPrimitive()));
					}
					else {
						configuration.getAutoMappingUnknownColumnBehavior()
								.doAction(mappedStatement, columnName, property, properType);
					}
				}
				else {
					configuration.getAutoMappingUnknownColumnBehavior()
							.doAction(mappedStatement, columnName, (property != null) ? property : propertyName, null);
				}
			}
			autoMappingsCache.put(mapKey, autoMapping);
		}
		return autoMapping;
	}

	private boolean applyAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
		List<UnMappedColumnAutoMapping> autoMapping = createAutomaticMappings(rsw, resultMap, metaObject, columnPrefix);
		boolean foundValues = false;
		if (!autoMapping.isEmpty()) {
			for (UnMappedColumnAutoMapping mapping : autoMapping) {
				Object value = mapping.typeHandler.getResult(rsw.getResultSet(), mapping.column);
				if (value != null) {
					foundValues = true;
				}
				if (value == null || (configuration.isCallSettersOnNulls() && !mapping.primitive)) {
					metaObject.setValue(mapping.property, value);
				}
			}
		}
		return foundValues;
	}

	private void linkToParents(ResultSet rs, ResultMapping parentMapping, Object rowValue) throws SQLException {
		CacheKey parentKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.getColumn(), parentMapping.getColumn());
		List<PendingRelation> parents = pendingRelations.get(parentKey);
		if (parents != null) {
			for (PendingRelation parent : parents) {
				if (parent != null && rowValue != null) {
					linkObjects(parent.metaObject, parent.propertyMapping, rowValue);
				}
			}
		}
	}

	private void addPendingChildRelation(ResultSet rs, MetaObject metaResultObject, ResultMapping parentMapping) throws SQLException {
		CacheKey cacheKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.getColumn(), parentMapping.getColumn());
		PendingRelation deferLoad = new PendingRelation();
		deferLoad.metaObject = metaResultObject;
		deferLoad.propertyMapping = parentMapping;
		List<PendingRelation> relations = MapUtil.computIfAbsent(pendingRelations, cacheKey, k -> new ArrayList<>());

		relations.add(deferLoad);
		ResultMapping previous = nextResultMaps.get(parentMapping.getResultSet());
		if (previous == null) {
			nextResultMaps.put(parentMapping.getResultSet(), parentMapping);
		}
		else {
			if (!previous.equals(parentMapping)) {
				throw new ExecutorException("Two different properties are mapped to the same resultSet");
			}
		}
	}

	private CacheKey createKeyForMultipleResults(ResultSet rs, ResultMapping resultMapping, String names, String columns) throws SQLException {
		CacheKey cacheKey = new CacheKey();
		cacheKey.update(resultMapping);

		if (columns != null && names != null) {
			String[] columnsArray = columns.split(",");
			String[] namesArray = names.split(",");
			for (int i = 0; i < columnsArray.length; i++) {
				Object value = rs.getString(columnsArray[i]);
				if (value != null) {
					cacheKey.update(namesArray[i]);
					cacheKey.update(value);
				}
			}
		}
		return cacheKey;
	}

	private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
		this.useConstructorMappings = false;
		List<Class<?>> constructorArgTypes = new ArrayList<>();
		List<Object> constructorArgs = new ArrayList<>();
		Object resultObject = createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);

		if (resultObject != null && !hasTypeHandlersForResultObject(rsw, resultMap.getType())) {
			List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
			for (ResultMapping propertyMapping : propertyMappings) {
				if (propertyMapping.getNestedQueryId() != null && propertyMapping.isLazy()) {
					resultObject = configuration.getProxyFactory().createProxy(resultObject, lazyLoader, configuration, objectFactory, constructorArgTypes, constructorArgs);
					break;
				}
			}
		}

		this.useConstructorMappings = resultObject != null && !constructorArgs.isEmpty();
		return resultObject;
	}

	private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) throws SQLException {
		Class<?> resultType = resultMap.getType();
		MetaClass metaType = MetaClass.forClass(resultType, reflectorFactory);
		List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();
		if (hasTypeHandlersForResultObject(rsw, resultType)) {
			return createPrimitiveResultObject(rsw, resultMap, columnPrefix);
		}
		else if (!constructorMappings.isEmpty()) {
			return createParameterizedResultObject(rsw, resultType, constructorMappings, constructorArgTypes, constructorArgs, columnPrefix);
		}
		else if (resultType.isInterface() || metaType.hasDefaultConstructor()) {
			return objectFactory.create(resultType);
		}
		else if (shouldApplyAutomaticMappings(resultMap, false)) {
			return createByConstructorSignature(rsw, resultMap, columnPrefix, resultType, constructorArgTypes, constructorArgs);
		}
		throw new ExecutorException("Do not know how to create an instance of " + resultType);
	}

	Object createParameterizedResultObject(ResultSetWrapper rsw, Class<?> resultType, List<ResultMapping> constructorMappings,
	                                       List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) {
		boolean foundValues = false;
		for (ResultMapping constructorMapping : constructorMappings) {
			Class<?> parameterType = constructorMapping.getJavaType();
			String column = constructorMapping.getColumn();
			Object value;

			try {
				if (constructorMapping.getNestedQueryId() != null) {
					value = getNestedQueryConstructorValue(rsw.getResultSet(), constructorMapping, columnPrefix);
				}
				else if (constructorMapping.getNestedResultMapId() != null) {
					ResultMap resultMap = configuration.getResultMap(constructorMapping.getNestedResultMapId());
					value = getRowValue(rsw, resultMap, getColumnPrefix(columnPrefix, constructorMapping));
				}
				else {
					TypeHandler<?> typeHandler = constructorMapping.getTypeHandler();
					value = typeHandler.getResult(rsw.getResultSet(), prependPrefix(column, columnPrefix));
				}
			}
			catch (ResultMapException | SQLException e) {
				throw new ExecutorException("Could not process result for mapping: " + constructorMapping, e);
			}

			constructorArgTypes.add(parameterType);
			constructorArgs.add(value);
			foundValues = value != null || foundValues;
		}
		return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
	}

	private Object createByConstructorSignature(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix, Class<?> resultType,
	                                            List<Class<?>> constructorArgTypes, List<Object> constructorArgs) throws SQLException {
		return applyConstructorAutoMapping(rsw, resultMap, columnPrefix, resultType, constructorArgTypes, constructorArgs,
				findConstructorForAutoMapping(resultType, rsw).orElseThrow(() -> new ExecutorException(
						"No constructor found in " + resultType.getName() + " matching " + rsw.getClassNames()
				)));
	}

	private Optional<Constructor<?>> findConstructorForAutoMapping(final Class<?> resultType, ResultSetWrapper rsw) {
		Constructor<?>[] constructors = resultType.getDeclaredConstructors();
		if (constructors.length == 1) {
			return Optional.of(constructors[0]);
		}
		for (Constructor<?> constructor : constructors) {
			if (constructor.isAnnotationPresent(AutomapConstructor.class)) {
				return Optional.of(constructor);
			}
		}
		if (configuration.isArgNameBasedConstructorAutoMapping()) {
			throw new ExecutorException(MessageFormat.format("'argNameBasedConstructorAutoMapping' is enabled and the class ''{0}'' has multiple constructors, so @AutomapConstructor must be added to one of the constructors.",
					resultType.getName()));
		}
		else {
			return Arrays.stream(constructors).filter(x -> findUsableConstructorByArgTypes(x, rsw.getJdbcTypes())).findAny();
		}

	}

	private boolean findUsableConstructorByArgTypes(final Constructor<?> constructor, final List<JdbcType> jdbcTypes) {
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		if (parameterTypes.length != jdbcTypes.size()) {
			return false;
		}

		for (int i = 0; i < parameterTypes.length; i++) {
			if (!typeHandlerRegistry.hasTypeHandler(parameterTypes[i], jdbcTypes.get(i))) {
				return false;
			}
		}
		return true;
	}

	private Object applyConstructorAutoMapping(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix, Class<?> resultType,
	                                           List<Class<?>> constructorArgTypes, List<Object> constructorArgs, Constructor<?> constructor) throws SQLException {
		boolean foundValues = false;
		if (configuration.isArgNameBasedConstructorAutoMapping()) {
			foundValues = applyArgNameBasedConstructorAutoMapping(rsw, resultMap, columnPrefix, resultType, constructorArgTypes, constructorArgs, constructor, foundValues);
		}
		else {
			foundValues = applyColumnOrderBasedConstructorAutoMapping(rsw, constructorArgTypes, constructorArgs, constructor, foundValues);
		}

		return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
	}

	private boolean applyColumnOrderBasedConstructorAutoMapping(ResultSetWrapper rsw, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, Constructor<?> constructor, boolean foundValues) throws SQLException {
		for (int i = 0; i < constructor.getParameterTypes().length; i++) {
			Class<?> parameterType = constructor.getParameterTypes()[i];
			String columnName = rsw.getColumnNames().get(i);
			TypeHandler<?> typeHandler = rsw.getTypeHandler(parameterType, columnName);
			Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
			constructorArgTypes.add(parameterType);
			constructorArgs.add(value);
			foundValues = value != null || foundValues;
		}
		return foundValues;
	}

	private boolean applyArgNameBasedConstructorAutoMapping(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix, Class<?> resultType,
	                                                        List<Class<?>> constructorArgTypes, List<Object> constructorArgs, Constructor<?> constructor, boolean foundValues) throws SQLException {
		List<String> missingArgs = null;
		Parameter[] params = constructor.getParameters();
		for (Parameter param : params) {
			boolean columnNotFound = true;
			Param paramAnno = param.getAnnotation(Param.class);
			String paramName = paramAnno == null ? param.getName() : paramAnno.value();
			for (String columnName : rsw.getColumnNames()) {
				if (columnMatchesParam(columnName, paramName, columnPrefix)) {
					Class<?> paramType = param.getType();
					TypeHandler<?> typeHandler = rsw.getTypeHandler(paramType, columnName);
					Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
					constructorArgTypes.add(paramType);
					constructorArgs.add(value);

					String mapKey = resultMap.getId() + ":" + columnPrefix;
					if (!autoMappingsCache.containsKey(mapKey)) {
						MapUtil.computIfAbsent(constructorAutoMappingColumns, mapKey, k -> new ArrayList<>()).add(columnName);
					}
					columnNotFound = false;
					foundValues = value != null || foundValues;
				}
			}

			if (columnNotFound) {
				if (missingArgs == null) {
					missingArgs = new ArrayList<>();
				}
				missingArgs.add(paramName);
			}
		}

		if (foundValues && constructorArgs.size() < params.length) {
			throw new ExecutorException(MessageFormat.format("Constructor auto-mapping of ''{1}'' failed because ''{0}'' were not found in the result set;"
			                                                 + "Available columns are ''{2}'' and mapUnderscoreToCamelCase is ''{3}''."
					, missingArgs,
					constructor,
					rsw.getColumnNames(),
					configuration.isMapUnderscoreToCamelCase()));
		}
		return foundValues;
	}

	private boolean columnMatchesParam(String columnName, String paramName, String columnPrefix) {
		if (columnPrefix != null) {
			if (!columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
				return false;
			}
			columnName = columnPrefix.substring(columnPrefix.length());
		}
		return paramName.equalsIgnoreCase(configuration.isMapUnderscoreToCamelCase() ? columnName.replace("_", "") : columnName);
	}

	private Object createPrimitiveResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
		Class<?> resultType = resultMap.getType();
		String columnName;
		if (!resultMap.getResultMappings().isEmpty()) {
			List<ResultMapping> resultMappingList = resultMap.getResultMappings();
			ResultMapping mapping = resultMappingList.get(0);
			columnName = prependPrefix(mapping.getColumn(), columnPrefix);
		}
		else {
			columnName = rsw.getColumnNames().get(0);
		}

		TypeHandler<?> typeHandler = rsw.getTypeHandler(resultType, columnName);
		return typeHandler.getResult(rsw.getResultSet(), columnName);
	}

	private Object getNestedQueryConstructorValue(ResultSet rs, ResultMapping constructorMapping, String columnPrefix) throws SQLException {
		String nestedQueryId = constructorMapping.getNestedQueryId();
		MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
		Class<?> nestedQueyrParameterType = nestedQuery.getParameterMap().getType();
		Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, constructorMapping, nestedQueyrParameterType, columnPrefix);
		Object value = null;

		if (nestedQueryParameterObject != null) {
			BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
			CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT, nestedBoundSql);
			Class<?> targetType = constructorMapping.getJavaType();
			ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, targetType, key, nestedBoundSql);
			value = resultLoader.loadResult();
		}
		return value;
	}

	private Object getNestedQueryMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
		String nestedQueryId = propertyMapping.getNestedQueryId();
		String property = propertyMapping.getProperty();
		MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
		Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType();
		Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, propertyMapping, nestedQueryParameterType, columnPrefix);
		Object value = null;

		if (nestedQueryParameterObject != null) {
			BoundSql nestedQueryBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
			CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT, nestedQueryBoundSql);
			Class<?> targetType = propertyMapping.getJavaType();
			if (executor.isCached(nestedQuery, key)) {
				executor.deferLoad(nestedQuery, metaResultObject, property, key, targetType);
				value = DEFERRED;
			}
			else {
				ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, targetType, key, nestedQueryBoundSql);
				if (propertyMapping.isLazy()) {
					lazyLoader.addLoader(property, metaResultObject, resultLoader);
					value = DEFERRED;
				}
				else {
					value = resultLoader.loadResult();
				}
			}
		}
		return value;
	}

	private Object prepareParameterForNestedQuery(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
		if (resultMapping.isCompositeResult()) {
			return prepareCompositeKeyParameter(rs, resultMapping, parameterType, columnPrefix);
		}
		else {
			return prepareSimpleKeyParameter(rs, resultMapping, parameterType, columnPrefix);
		}
	}

	private Object prepareSimpleKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
		final TypeHandler<?> typeHandler;
		if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
			typeHandler = typeHandlerRegistry.getTypeHandler(parameterType);
		}
		else {
			typeHandler = typeHandlerRegistry.getUnknownTypeHandler();
		}
		return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
	}

	private Object prepareCompositeKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
		Object parameterObject = instantiateParameterObject(parameterType);
		MetaObject metaObject = configuration.newMetaObject(parameterObject);
		boolean foundValues = false;
		for (ResultMapping innerResultMapping : resultMapping.getComposites()) {
			Class<?> propType = metaObject.getSetterType(innerResultMapping.getProperty());
			TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(propType);
			Object propValue = typeHandler.getResult(rs, prependPrefix(innerResultMapping.getColumn(), columnPrefix));
			if (propValue != null) {
				metaObject.setValue(innerResultMapping.getProperty(), propValue);
				foundValues = true;
			}
		}
		return foundValues ? parameterObject : null;
	}

	private Object instantiateParameterObject(Class<?> parameterType) {
		if (parameterType == null) {
			return new HashMap<>();
		}
		else if (MapperMethod.ParamMap.class.equals(parameterType)) {
			return new HashMap<>();
		}
		else {
			return objectFactory.create(parameterType);
		}
	}

	public ResultMap resolveDiscriminatedResultMap(ResultSet rs, ResultMap resultMap, String columnPrefix) throws SQLException {
		Set<String> pastDiscriminators = new HashSet<>();
		Discriminator discriminator = resultMap.getDiscriminator();
		while (discriminator != null) {
			Object value = getDiscriminatorValue(rs, discriminator, columnPrefix);
			String discriminatorMapId = discriminator.getMapIdFor(String.valueOf(value));
			if (configuration.hasResultMap(discriminatorMapId)) {
				resultMap = configuration.getResultMap(discriminatorMapId);
				Discriminator lastDiscriminator = discriminator;
				discriminator = resultMap.getDiscriminator();
				if (discriminator == lastDiscriminator || !pastDiscriminators.add(discriminatorMapId)) {
					break;
				}
			}
			else {
				break;
			}
		}
		return resultMap;
	}

	private Object getDiscriminatorValue(ResultSet rs, Discriminator discriminator, String columnPrefix) throws SQLException {
		ResultMapping resultMapping = discriminator.getResultMapping();
		TypeHandler<?> typeHandler = resultMapping.getTypeHandler();
		return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
	}

	private String prependPrefix(String columnName, String prefix) {
		if (columnName == null || columnName.length() == 0 || prefix == null || prefix.length() == 0) {
			return columnName;
		}

		return prefix + columnName;
	}

	private void handleRowValuesForNestedResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
		final DefaultResultContext<Object> resultContext = new DefaultResultContext<>();
		ResultSet resultSet = rsw.getResultSet();
		skipRows(resultSet, rowBounds);
		Object rowValue = previousRowValue;

		while (shouldProcessMoreRows(resultContext, rowBounds) && !resultSet.isClosed() && resultSet.next()) {
			final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(resultSet, resultMap, null);
			final CacheKey rowKey = createRowKey(discriminatedResultMap, rsw, null);
			Object partialObject = nestedResultObjects.get(rowKey);

			if (mappedStatement.isResultOrdered()) {
				if (partialObject == null && rowValue != null) {
					nestedResultObjects.clear();
					storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet);
				}
				rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject);
			}
			else {
				rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject);
				if (partialObject == null) {
					storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet);
				}
			}
		}

		if (rowValue != null && mappedStatement.isResultOrdered() && shouldProcessMoreRows(resultContext, rowBounds)) {
			storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet);
			previousRowValue = null;
		}
		else if (rowValue != null) {
			previousRowValue = rowValue;
		}
	}

	private boolean applyNestedResultMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String parentPrefix, CacheKey parentRowKey, boolean newObject) {
		boolean foundValues = false;
		for (ResultMapping resultMapping : resultMap.getPropertyResultMappings()) {
			String nestedResultMapId = resultMapping.getNestedResultMapId();
			if (nestedResultMapId != null && resultMapping.getResultSet() == null) {
				try {
					String columnPrefix = getColumnPrefix(parentPrefix, resultMapping);
					ResultMap nestedResultMap = getNestedResultMap(rsw.getResultSet(), nestedResultMapId, columnPrefix);
					if (resultMapping.getColumnPrefix() == null) {
						Object ancestorObject = ancestorObjects.get(nestedResultMapId);
						if (ancestorObject != null) {
							if (newObject) {
								linkObjects(metaObject, resultMapping, ancestorObject);
							}
							continue;
						}
					}

					final CacheKey rowKey = createRowKey(nestedResultMap, rsw, columnPrefix);
					final CacheKey combinedKey = combineKeys(rowKey, parentRowKey);
					Object rowValue = nestedResultObjects.get(combinedKey);
					boolean knownValue = rowValue != null;
					instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject);

					if (anyNotNullColumnHasValue(resultMapping, columnPrefix, rsw)) {
						rowValue = getRowValue(rsw, nestedResultMap, combinedKey, columnPrefix, rowValue);
						if (rowValue != null && !knownValue) {
							linkObjects(metaObject, resultMapping, rowValue);
							foundValues = true;
						}
					}
				}
				catch (SQLException e) {
					throw new ExecutorException("Error getting nested result map values for '" + resultMapping.getProperty() + "'. Cause: " + e, e);
				}
			}
		}
		return foundValues;
	}

	private String getColumnPrefix(String parentPrefix, ResultMapping resultMapping) {
		final StringBuilder columnPrefixBuilder = new StringBuilder();
		if (parentPrefix != null) {
			columnPrefixBuilder.append(parentPrefix);
		}

		if (resultMapping.getColumnPrefix() != null) {
			columnPrefixBuilder.append(resultMapping.getColumnPrefix());
		}

		return columnPrefixBuilder.length() == 0 ? null : columnPrefixBuilder.toString().toUpperCase(Locale.ENGLISH);
	}

	private boolean anyNotNullColumnHasValue(ResultMapping resultMapping, String columnPrefix, ResultSetWrapper rsw) throws SQLException {
		Set<String> notNullColumns = resultMapping.getNotNullColumns();
		if (notNullColumns != null && !notNullColumns.isEmpty()) {
			ResultSet rs = rsw.getResultSet();
			for (String column : notNullColumns) {
				rs.getObject(prependPrefix(column, columnPrefix));
				if (!rs.wasNull()) {
					return true;
				}
			}
			return false;
		}
		else if (columnPrefix != null) {
			for (String columnName : rsw.getColumnNames()) {
				if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix.toUpperCase(Locale.ENGLISH))) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private ResultMap getNestedResultMap(ResultSet rs, String nestedResultMapId, String columnPrefix) throws SQLException {
		ResultMap nestedResultMap = configuration.getResultMap(nestedResultMapId);
		return resolveDiscriminatedResultMap(rs, nestedResultMap, columnPrefix);
	}

	private CacheKey createRowKey(ResultMap resultMap, ResultSetWrapper rsw, String columnPrefix) throws SQLException {
		final CacheKey cacheKey = new CacheKey();
		cacheKey.update(resultMap.getId());
		List<ResultMapping> resultMappings = getResultMappingsForRowKey(resultMap);
		if (resultMappings.isEmpty()) {
			if (Map.class.isAssignableFrom(resultMap.getType())) {
				createRowKeyForMap(rsw, cacheKey);
			}
			else {
				createRowKeyForUnmappedProperties(resultMap, rsw, cacheKey, columnPrefix);
			}
		}
		else {
			createRowKeyForMappedProperties(resultMap, rsw, cacheKey, resultMappings, columnPrefix);
		}

		if (cacheKey.getUpdateCount() < 2) {
			return CacheKey.NULL_CACHE_KEY;
		}
		return cacheKey;
	}

	private CacheKey combineKeys(CacheKey rowKey, CacheKey parentRowKey) {
		if (rowKey.getUpdateCount() > 1 && parentRowKey.getUpdateCount() > 1) {
			CacheKey combinedKey;
			try {
				combinedKey = rowKey.clone();
			}
			catch (CloneNotSupportedException e) {
				throw new ExecutorException("Error cloning cache key. Cause: " + e, e);
			}
			combinedKey.update(parentRowKey);
			return combinedKey;
		}
		return CacheKey.NULL_CACHE_KEY;
	}

	private List<ResultMapping> getResultMappingsForRowKey(ResultMap resultMap) {
		List<ResultMapping> resultMappings = resultMap.getIdResultMappings();
		if (resultMappings.isEmpty()) {
			resultMappings = resultMap.getPropertyResultMappings();
		}
		return resultMappings;
	}

	private void createRowKeyForMappedProperties(ResultMap resultMap, ResultSetWrapper rsw, CacheKey cacheKey, List<ResultMapping> resultMappings, String columnPrefix) throws SQLException {
		for (ResultMapping resultMapping : resultMappings) {
			if (resultMapping.isSimple()) {
				final String column = prependPrefix(resultMapping.getColumn(), columnPrefix);
				final TypeHandler<?> th = resultMapping.getTypeHandler();
				List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix);

				if (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) {
					final Object value = th.getResult(rsw.getResultSet(), column);
					if (value != null || configuration.isReturnInstanceForEmptyRow()) {
						cacheKey.update(column);
						cacheKey.update(value);
					}
				}
			}
		}
	}

	private void createRowKeyForUnmappedProperties(ResultMap resultMap, ResultSetWrapper rsw, CacheKey cacheKey, String columnPrefix) throws SQLException {
		final MetaClass metaType = MetaClass.forClass(resultMap.getType(), reflectorFactory);
		List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);

		for (String column : unmappedColumnNames) {
			String property = column;
			if (columnPrefix != null && !columnPrefix.isEmpty()) {
				if (column.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
					property = column.substring(columnPrefix.length());
				}
				else {
					continue;
				}
			}

			if (metaType.findProperty(property, configuration.isMapUnderscoreToCamelCase()) != null) {
				String value = rsw.getResultSet().getString(column);
				if (value != null) {
					cacheKey.update(column);
					cacheKey.update(value);
				}
			}
		}
	}

	private void createRowKeyForMap(ResultSetWrapper rsw, CacheKey cacheKey) throws SQLException {
		List<String> columnNames = rsw.getColumnNames();
		for (String columnName : columnNames) {
			final String value = rsw.getResultSet().getString(columnName);
			if (value != null) {
				cacheKey.update(columnName);
				cacheKey.update(value);
			}
		}
	}

	private void linkObjects(MetaObject metaObject, ResultMapping resultMapping, Object rowValue) {
		final Object collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject);
		if (collectionProperty != null) {
			final MetaObject targetMetaObject = configuration.newMetaObject(collectionProperty);
			targetMetaObject.add(rowValue);
		}
		else {
			metaObject.setValue(resultMapping.getProperty(), rowValue);
		}
	}

	private Object instantiateCollectionPropertyIfAppropriate(ResultMapping resultMapping, MetaObject metaObject) {
		final String propertyName = resultMapping.getProperty();
		Object propertyValue = metaObject.getValue(propertyName);
		if (propertyValue == null) {
			Class<?> type = resultMapping.getJavaType();
			if (type == null) {
				type = metaObject.getSetterType(propertyName);
			}

			try {
				if (objectFactory.isCollection(type)) {
					propertyValue = objectFactory.create(type);
					metaObject.setValue(propertyName, propertyValue);
					return propertyValue;
				}
			}
			catch (Exception e) {
				throw new ExecutorException("Error instantiating collection property for result '" + resultMapping.getProperty() + "'. Cause: " + e, e);
			}
		}
		else if (objectFactory.isCollection(propertyValue.getClass())) {
			return propertyValue;
		}
		return null;
	}

	private boolean hasTypeHandlersForResultObject(ResultSetWrapper rsw, Class<?> resultType) {
		if (rsw.getColumnNames().size() == 1) {
			return typeHandlerRegistry.hasTypeHandler(resultType, rsw.getJdbcType(rsw.getColumnNames().get(0)));
		}
		return typeHandlerRegistry.hasTypeHandler(resultType);
	}

	private static class PendingRelation {
		private MetaObject metaObject;
		private ResultMapping propertyMapping;
	}

	private static class UnMappedColumnAutoMapping {
		private final String column;
		private final String property;
		private final TypeHandler<?> typeHandler;
		private final boolean primitive;

		public UnMappedColumnAutoMapping(String column, String property, TypeHandler<?> typeHandler, boolean primitive) {
			this.column = column;
			this.property = property;
			this.typeHandler = typeHandler;
			this.primitive = primitive;
		}
	}
}
