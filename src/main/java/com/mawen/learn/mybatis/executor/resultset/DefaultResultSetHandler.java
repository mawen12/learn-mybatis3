package com.mawen.learn.mybatis.executor.resultset;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mawen.learn.mybatis.cache.CacheKey;
import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.executor.parameter.ParameterHandler;
import com.mawen.learn.mybatis.executor.result.DefaultResultContext;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.ResultMap;
import com.mawen.learn.mybatis.mapping.ResultMapping;
import com.mawen.learn.mybatis.reflection.MetaClass;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.ReflectorFactory;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;

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
				createRowKeyForMap(rsw,cacheKey);
			}
			else {
				createRowKeyForUnmappedProperties(resultMap, rsw,cacheKey,columnPrefix);
			}
		}
		else {
			createRowKeyForMappedProperties(resultMap,rsw,cacheKey,resultMappings,columnPrefix);
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
