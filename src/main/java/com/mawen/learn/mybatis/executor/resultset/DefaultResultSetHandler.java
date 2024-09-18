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
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.executor.loader.ResultLoader;
import com.mawen.learn.mybatis.executor.loader.ResultLoaderMap;
import com.mawen.learn.mybatis.executor.parameter.ParameterHandler;
import com.mawen.learn.mybatis.executor.result.DefaultResultContext;
import com.mawen.learn.mybatis.executor.result.ResultMapException;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.Discriminator;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.ResultMap;
import com.mawen.learn.mybatis.mapping.ResultMapping;
import com.mawen.learn.mybatis.reflection.MetaClass;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.ReflectorFactory;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.session.AutoMappingBehavior;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.ObjectTypeHandler;
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


	@Override
	public <E> List<E> handleResultSets(Statement statement) throws SQLException {
		return List.of();
	}

	@Override
	public <E> Cursor<E> handleCursorResultSets(Statement statement) throws SQLException {
		return null;
	}

	@Override
	public void handleOutputParameters(CallableStatement cs) throws SQLException {

	}

	private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) {

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
	                                                        List<Class<?>> constructorArgTypes, List<Object> constructorArgs, Constructor<?> constructor, boolean foundValues) {
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

	private Object prepareCompositeKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) {
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

	private void handleRowValuesForNestedResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping resultMapping) {
		final DefaultResultContext<Object> resultContext = new DefaultResultContext<>();
		ResultSet resultSet = rsw.getResultSet();
		skipRows(resultSet, rowBounds);
		Object rowValue = previousRowValue;

		while (shouldProcessMoreRows(resultContext, rowBounds) && !resultSet.isClosed() && resultSet.next()) {
			final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(resultSet, resultMap, null);
			final CacheKey rowKey = createRowKey(discriminatedResultMap, rsw, null);
			Object partialObject = nestedResultObjects.get(rowKey);

			if (mappedStatement.isResultOrdered()) {

			}
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

	private ResultMap getNestedResultMap(ResultSet rs, String nestedResultMapId, String columnPrefix) {
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
		private ResultMapping resultMapping;
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
