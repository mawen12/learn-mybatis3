package com.mawen.learn.mybatis.executor.resultset;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.mapping.ResultMap;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.ObjectTypeHandler;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;
import com.mawen.learn.mybatis.type.UnknownTypeHandler;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class ResultSetWrapper {

	private final ResultSet resultSet;
	private final TypeHandlerRegistry typeHandlerRegistry;
	private final List<String> columnNames = new ArrayList<>();
	private final List<String> classNames = new ArrayList<>();
	private final List<JdbcType> jdbcTypes = new ArrayList<>();
	private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();
	private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
	private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

	public ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
		super();
		this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
		this.resultSet = rs;

		final ResultSetMetaData metaData = rs.getMetaData();
		final int columnCount = metaData.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			columnNames.add(configuration.isUseColumnLabel() ? metaData.getColumnLabel(i) : metaData.getColumnName(i));
			jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
			classNames.add(metaData.getColumnClassName(i));
		}
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public List<String> getClassNames() {
		return Collections.unmodifiableList(classNames);
	}

	public List<JdbcType> getJdbcTypes() {
		return jdbcTypes;
	}

	public JdbcType getJdbcType(String columnName) {
		for (int i = 0; i < columnNames.size(); i++) {
			if (columnNames.get(i).equalsIgnoreCase(columnName)) {
				return jdbcTypes.get(i);
			}
		}
		return null;
	}

	public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
		TypeHandler<?> handler = null;
		Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
		if (columnHandlers == null) {
			columnHandlers = new HashMap<>();
			typeHandlerMap.put(columnName, columnHandlers);
		}
		else {
			handler = columnHandlers.get(propertyType);
		}

		if (handler == null) {
			JdbcType jdbcType = getJdbcType(columnName);
			handler = typeHandlerRegistry.getTypeHandler(propertyType, jdbcType);

			if (handler == null || handler instanceof UnknownTypeHandler) {
				final int index = classNames.indexOf(columnName);
				final Class<?> javaType = resolveClass(classNames.get(index));
				if (javaType != null && jdbcType != null) {
					handler = typeHandlerRegistry.getTypeHandler(javaType, jdbcType);
				}
				else if (javaType != null) {
					handler = typeHandlerRegistry.getTypeHandler(javaType);
				}
				else if (jdbcType != null) {
					handler = typeHandlerRegistry.getTypeHandler(jdbcType);
				}
			}

			if (handler == null || handler instanceof UnknownTypeHandler) {
				handler = new ObjectTypeHandler();
			}

			columnHandlers.put(propertyType, handler);
		}
		return handler;
	}

	private Class<?> resolveClass(String className) {
		try {
			if (className != null) {
				return Resources.classForName(className);
			}
		}
		catch (ClassNotFoundException ingored) {}

		return null;
	}

	private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) {
		List<String> mappedColumnNames = new ArrayList<>();
		List<String> unmappedColumnNames = new ArrayList<>();
		final String upperColumnPrefix = columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);
		final Set<String> mappedColumns = prependPrefixes(resultMap.getMappedColumns(), upperColumnPrefix);
		for (String columnName : columnNames) {
			String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
			if (mappedColumns.contains(upperColumnName)) {
				mappedColumnNames.add(upperColumnName);
			}
			else {
				unmappedColumnNames.add(upperColumnName);
			}
		}
		mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumnNames);
		unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
	}

	public List<String> getMappedColumnNames(ResultMap resultMap, String columnPrefix) {
		List<String> mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
		if (mappedColumnNames == null) {
			loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
			mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
		}
		return mappedColumnNames;
	}

	public List<String> getUnmappedColumnNames(ResultMap resultMap, String columnPrefix) {
		List<String> unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
		if (unMappedColumnNames == null) {
			loadMappedAndUnMappedColumnNames(resultMap, columnPrefix);
			unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
		}
		return unMappedColumnNames;
	}

	private Set<String> prependPrefixes(Set<String> columnNames, String prefix) {
		if (columnNames == null || columnNames.isEmpty() || prefix == null || prefix.length() == 0) {
			return columnNames;
		}

		final Set<String> prefixed = new HashSet<>();
		for (String className : classNames) {
			prefixed.add(prefix + className);
		}
		return prefixed;
	}
}
