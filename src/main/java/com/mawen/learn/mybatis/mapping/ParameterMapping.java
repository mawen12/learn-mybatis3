package com.mawen.learn.mybatis.mapping;

import java.sql.ResultSet;

import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public class ParameterMapping {

	private Configuration configuration;

	private String property;
	private ParameterMode mode;
	private Class<?> javaType = Object.class;
	private JdbcType jdbcType;
	private Integer numericScale;
	private TypeHandler<?> typeHandler;
	private String resultMapId;
	private String jdbcTypeName;
	private String expression;

	private ParameterMapping() {
	}

	public static class Builder {
		private ParameterMapping parameterMapping = new ParameterMapping();

		public Builder(Configuration configuration, String property, TypeHandler<?> typeHandler) {
			parameterMapping.configuration = configuration;
			parameterMapping.property = property;
			parameterMapping.typeHandler = typeHandler;
			parameterMapping.mode = ParameterMode.IN;
		}

		public Builder(Configuration configuration, String property, Class<?> javaType) {
			parameterMapping.configuration = configuration;
			parameterMapping.property = property;
			parameterMapping.javaType = javaType;
			parameterMapping.mode = ParameterMode.IN;
		}

		public Builder mode(ParameterMode mode) {
			parameterMapping.mode = mode;
			return this;
		}

		public Builder javaType(Class<?> javaType) {
			parameterMapping.javaType = javaType;
			return this;
		}

		public Builder jdbcType(JdbcType jdbcType) {
			parameterMapping.jdbcType = jdbcType;
			return this;
		}

		public Builder numericScale(Integer numericScale) {
			parameterMapping.numericScale = numericScale;
			return this;
		}

		public Builder resultMapId(String resultMapId) {
			parameterMapping.resultMapId = resultMapId;
			return this;
		}

		public Builder typeHandler(TypeHandler<?> typeHandler) {
			parameterMapping.typeHandler = typeHandler;
			return this;
		}

		public Builder jdbcTypeName(String jdbcTypeName) {
			parameterMapping.jdbcTypeName = jdbcTypeName;
			return this;
		}

		public Builder expression(String expression) {
			parameterMapping.expression = expression;
			return this;
		}

		public ParameterMapping build() {
			resolveTypeHandler();
			validate();
			return parameterMapping;
		}

		private void validate() {
			if (ResultSet.class.equals(parameterMapping.javaType)) {
				if (parameterMapping.resultMapId == null) {
					throw new IllegalStateException("Missing resultMap in property '" + parameterMapping.property + "'." +
					                                "Parameters of type java.sql.ResultSet require a resultMap.");
				}
			}
			else {
				if (parameterMapping.typeHandler == null) {
					throw new IllegalStateException("Type Handler was null on parameter mapping for property '" + parameterMapping.property + "'." +
					                                "It was either not specified and/or could not be found for the javaType (" + parameterMapping.javaType.getName() +
					                                ") : jdbcType (" + parameterMapping.jdbcType + ") combination.");
				}
			}
		}

		private void resolveTypeHandler() {
			if (parameterMapping.typeHandler == null && parameterMapping.javaType != null) {
				Configuration configuration = parameterMapping.configuration;
				TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

			}
		}
	}

}