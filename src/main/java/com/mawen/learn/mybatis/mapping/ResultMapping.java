package com.mawen.learn.mybatis.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;
import sun.security.krb5.Config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/1
 */
public class ResultMapping {

	private Configuration configuration;
	private String property;
	private String column;
	private Class<?> javaType;
	private Class<?> jdbcType;
	private TypeHandler<?> typeHandler;
	private String nestedResultMapId;
	private String nestedQueryId;
	private Set<String> notNulColumns;
	private String columnPrefix;
	private List<ResultFlag> flags;
	private List<ResultMapping> composites;
	private String resultSet;
	private String foreignColumn;
	private boolean lazy;

	ResultMapping() {}

	public static class Builder {

		private ResultMapping resultMapping = new ResultMapping();

		public Builder(Configuration configuration, String property, String column, TypeHandler<?> typeHandler) {
			this(configuration, property);
			resultMapping.column = column;
			resultMapping.typeHandler = typeHandler;
		}

		public Builder(Configuration configuration, String property, String column, Class<?> javaType) {
			this(configuration, property);
			resultMapping.column = column;
			resultMapping.javaType = javaType;
		}

		public Builder(Configuration configuration, String property) {
			resultMapping.configuration = configuration;
			resultMapping.property = property;
			resultMapping.flags = new ArrayList<>();
			resultMapping.composites = new ArrayList<>();
			resultMapping.lazy = configuration.isLazyLoadingEnabled();
		}

		public Builder javaType(Class<?> javaType) {
			resultMapping.javaType = javaType;
			return this;
		}

		public Builder jdbcType(Class<?> jdbcType) {
			resultMapping.jdbcType = jdbcType;
			return this;
		}

		public Builder nestedResultMapId(String nestedResultMapId) {
			resultMapping.nestedResultMapId = nestedResultMapId;
			return this;
		}

		public Builder nestedQueryId(String nestedQueryId) {
			resultMapping.nestedQueryId = nestedQueryId;
			return this;
		}

		public Builder resultSet(String resultSet) {
			resultMapping.resultSet = resultSet;
			return this;
		}

		public Builder foreignColumn(String foreignColumn) {
			resultMapping.foreignColumn = foreignColumn;
			return this;
		}

		public Builder notNulColumns(Set<String> notNulColumns) {
			resultMapping.notNulColumns = notNulColumns;
			return this;
		}

		public Builder columnPrefix(String columnPrefix) {
			resultMapping.columnPrefix = columnPrefix;
			return this;
		}

		public Builder flags(List<ResultFlag> flags) {
			resultMapping.flags = flags;
			return this;
		}

		public Builder typeHandler(TypeHandler<?> typeHandler) {
			resultMapping.typeHandler = typeHandler;
			return this;
		}

		public Builder composites(List<ResultMapping> composites) {
			resultMapping.composites = composites;
			return this;
		}

		public Builder lazy(boolean lazy) {
			resultMapping.lazy = lazy;
			return this;
		}

		public ResultMapping build() {
			resultMapping.flags = Collections.unmodifiableList(resultMapping.flags);
			resultMapping.composites = Collections.unmodifiableList(resultMapping.composites);
			resolveTypeHandler();
			validate();
			return resultMapping;
		}

		private void resolveTypeHandler() {
			if (resultMapping.typeHandler == null && resultMapping.javaType == null) {
				Configuration configuration = resultMapping.configuration;
				TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
				resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, resultMapping.jdbcType);
			}
		}

		private void validate() {
			if (resultMapping.nestedQueryId != null && resultMapping.nestedResultMapId != null) {
				throw new IllegalStateException("Cannot define");
			}
		}

	}
}
