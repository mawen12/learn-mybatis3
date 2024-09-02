package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public class UnknownTypeHandler extends BaseTypeHandler<Object> {

	private static final ObjectTypeHandler OBJECT_TYPE_HANDLER = new ObjectTypeHandler();

	private final Configuration configuration;

	private final Supplier<TypeHandlerRegistry> typeHandlerRegistrySupplier;


	public UnknownTypeHandler(Configuration configuration) {
		this.configuration = configuration;
		this.typeHandlerRegistrySupplier = configuration::getTypeHandlerRegistry;
	}

	public UnknownTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
		this.configuration = new Configuration();
		this.typeHandlerRegistrySupplier = () -> typeHandlerRegistry;
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
		TypeHandler handler = resolveTypeHandler(parameter, jdbcType);
		handler.setParameter(ps, i,parameter,jdbcType);
	}

	@Override
	public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return null;
	}

	@Override
	public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}

	private TypeHandler<?> resolveTypeHandler(Object parameter, JdbcType jdbcType) {
		TypeHandler<?> handler;
		if (parameter == null) {
			handler = OBJECT_TYPE_HANDLER;
		}
		else {
			handler = typeHandlerRegistrySupplier.get().getTypeHandler(parameter.getClass(), jdbcType);
			if (handler == null || handler instanceof UnknownTypeHandler) {
				handler = OBJECT_TYPE_HANDLER;
			}
		}
		return handler;
	}

	private TypeHandler<?> resolveTypeHandler(ResultSet rs, String column) {
		try {
			Map<String, Integer> columnIndexLookup = new HashMap<>();
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			boolean useColumnLabel = configuration.isUseColumnLabel();

			for (int i = 0; i < count; i++) {
				String name = useColumnLabel ? rsmd.getColumnLabel(i) : rsmd.getColumnName(i);
				columnIndexLookup.put(name, i);
			}

			Integer columnIndex = columnIndexLookup.get(column);
			TypeHandler<?> handler = null;
			if (columnIndex != null) {
				handler = resolveTypeHandler(rsmd, columnIndex);
			}
			if (handler == null || handler instanceof UnknownTypeHandler) {
				handler = OBJECT_TYPE_HANDLER;
			}
			return handler;
		}
		catch (SQLException e) {
			throw new TypeException("Error determining JDBC type for column "+ column + ". Cause: " + e, e);
		}
	}

	private TypeHandler<?> resolveTypeHandler(ResultSetMetaData rsmd, Integer columnIndex) {
		TypeHandler<?> handler = null;
		JdbcType jdbcType = safeGetJdbcTypeForColumn(rsmd, columnIndex);
		Class<?> javaType = safeGetClassForColumn(rsmd, columnIndex);

		if (javaType != null && jdbcType != null) {
			handler = typeHandlerRegistrySupplier.get().getTypeHandler(javaType, jdbcType);
		}
		else if (javaType != null) {
			handler = typeHandlerRegistrySupplier.get().getTypeHandler(javaType);
		}
		else if (jdbcType != null) {
			handler = typeHandlerRegistrySupplier.get().getTypeHandler(jdbcType);
		}
		return handler;
	}

	private JdbcType safeGetJdbcTypeForColumn(ResultSetMetaData rsmd, Integer columnIndex) {
		try {
			return JdbcType.forCode(rsmd.getColumnType(columnIndex));
		}
		catch (SQLException e) {
			return null;
		}
	}

	private Class<?> safeGetClassForColumn(ResultSetMetaData rsmd, Integer columnIndex) {
		try {
			return Resources.classForName(rsmd.getColumnClassName(columnIndex));
		}
		catch (Exception e) {
			return null;
		}
	}
}
