package com.mawen.learn.mybatis.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;

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
	private Set<String> notNullColumns;
	private String columnPrefix;
	private List<ResultFlag> flags;
	private List<ResultMapping> composites;
	private String resultSet;
	private String foreignColumn;
	private boolean lazy;

	ResultMapping() {}


	public Configuration getConfiguration() {
		return configuration;
	}

	public String getProperty() {
		return property;
	}

	public String getColumn() {
		return column;
	}

	public Class<?> getJavaType() {
		return javaType;
	}

	public Class<?> getJdbcType() {
		return jdbcType;
	}

	public TypeHandler<?> getTypeHandler() {
		return typeHandler;
	}

	public String getNestedResultMapId() {
		return nestedResultMapId;
	}

	public String getNestedQueryId() {
		return nestedQueryId;
	}

	public Set<String> getNotNullColumns() {
		return notNullColumns;
	}

	public String getColumnPrefix() {
		return columnPrefix;
	}

	public List<ResultFlag> getFlags() {
		return flags;
	}

	public List<ResultMapping> getComposites() {
		return composites;
	}

	public boolean isCompositeResult() {
		return this.composites != null && !this.composites.isEmpty();
	}

	public String getResultSet() {
		return resultSet;
	}

	public String getForeignColumn() {
		return foreignColumn;
	}

	public boolean isLazy() {
		return lazy;
	}

	public boolean isSimple() {
		return this.nestedResultMapId == null && this.nestedQueryId == null && this.resultSet == null;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ResultMapping)) return false;

		ResultMapping that = (ResultMapping) o;
		return Objects.equals(property, that.property);
	}

	@Override
	public int hashCode() {
		if (property != null) {
			return property.hashCode();
		}
		else if (column != null) {
			return column.hashCode();
		}
		else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "ResultMapping{" +
		       ", property='" + property + '\'' +
		       ", column='" + column + '\'' +
		       ", javaType=" + javaType +
		       ", jdbcType=" + jdbcType +
		       ", nestedResultMapId='" + nestedResultMapId + '\'' +
		       ", nestedQueryId='" + nestedQueryId + '\'' +
		       ", notNulColumns=" + notNullColumns +
		       ", columnPrefix='" + columnPrefix + '\'' +
		       ", flags=" + flags +
		       ", composites=" + composites +
		       ", resultSet='" + resultSet + '\'' +
		       ", foreignColumn='" + foreignColumn + '\'' +
		       ", lazy=" + lazy +
		       '}';
	}

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
			resultMapping.notNullColumns = notNulColumns;
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

		public Builder column(String column) {
			resultMapping.column = column;
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

			if (resultMapping.nestedQueryId == null && resultMapping.nestedResultMapId == null && resultMapping.typeHandler == null) {
				throw new IllegalStateException("No typehanlder found for property " + resultMapping.property);
			}

			if (resultMapping.nestedResultMapId == null && resultMapping.column == null && resultMapping.composites.isEmpty()) {
				throw new IllegalStateException("Mapping is missing column attribute for property " + resultMapping.property);
			}

			if (resultMapping.getResultSet() != null) {
				int numColumns = 0;
				if (resultMapping.column != null) {
					numColumns = resultMapping.column.split(",").length;
				}
				int numForeignColumns = 0;
				if (resultMapping.foreignColumn != null) {
					numForeignColumns = resultMapping.foreignColumn.split(",").length;
				}
				if (numColumns != numForeignColumns) {
					throw new IllegalStateException("There should be the same number of columns and foreignColumns in property " + resultMapping.property);
				}
			}
		}
	}



}
