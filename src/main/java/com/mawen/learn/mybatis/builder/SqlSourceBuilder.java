package com.mawen.learn.mybatis.builder;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.mapping.ParameterMapping;
import com.mawen.learn.mybatis.mapping.SqlSource;
import com.mawen.learn.mybatis.parsing.TokenHandler;
import com.mawen.learn.mybatis.reflection.MetaClass;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.type.JdbcType;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class SqlSourceBuilder extends BaseBuilder {


	private static final String PARAMETER_PROPERTIES = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

	public SqlSourceBuilder(Configuration configuration) {
		super(configuration);
	}

	public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
		new ParameterMappingTokenHandler();
	}


	private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

		private final List<ParameterMapping> parameterMappings = new ArrayList<>();
		private final Class<?> parameterType;
		private final MetaObject metaParameters;

		public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
			super(configuration);
			this.parameterType = parameterType;
			this.metaParameters = configuration.newMetaObject(additionalParameters);
		}

		public List<ParameterMapping> getParameterMappings() {
			return parameterMappings;
		}

		@Override
		public String handleToken(String content) {
			parameterMappings.add(buildParameterMapping(content));
			return "?";
		}

		private ParameterMapping buildParameterMapping(String content) {
			Map<String, String> propertiesMap = parseParameterMapping(content);
			String property = propertiesMap.get("property");
			Class<?> propertyType;

			if (metaParameters.hasGetter(property)) {
				propertyType = metaParameters.getGetterType(property);
			}
			else if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
				propertyType = parameterType;
			}
			else if (JdbcType.CURSOR.name().equals(propertiesMap.get("jdbcType"))) {
				propertyType = ResultSet.class;
			}
			else if (property == null || Map.class.isAssignableFrom(parameterType)) {
				propertyType = Object.class;
			}
			else {
				MetaClass metaClass = MetaClass.forClass(parameterType, configuration.getReflectorFactory());
				if (metaClass.hasGetter(property)) {
					propertyType = metaClass.getGetterType(property);
				}
				else {
					propertyType = Object.class;
				}
			}

			ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
			Class<?> javaType = propertyType;
			String typeHandlerAlias = null;

			for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();

				if ("javaType".equals(name)) {
					javaType = resolveClass(value);
					builder.javaType(javaType);
				}
				else if ("jdbcType".equals(name)) {
					builder.jdbcType(resolveJdbcType(value));
				}
				else if ("mode".equals(name)) {
					builder.mode(resolveParameterMode(value));
				}
				else if ("numericScale".equals(name)) {
					builder.numericScale(Integer.valueOf(value));
				}
				else if ("resultMap".equals(name)) {
					builder.resultMapId(value);
				}
				else if ("typeHandler".equals(name)) {
					typeHandlerAlias = value;
				}
				else if ("jdbcTypeName".equals(name)) {
					builder.jdbcTypeName(value);
				}
				else if ("property".equals(name)) {
					// NOP
				}
				else if ("expression".equals(name)) {
					throw new BuilderException("Expression based parameters are not supported yet");
				}
				else {
					throw new BuilderException("An invalid property '" + name + "' was found in mapping #{" + content + "}. Valid properties are " + PARAMETER_PROPERTIES);
				}
			}

			if (typeHandlerAlias != null) {
				builder.typeHandler(resolveTypeHandler(javaType, typeHandlerAlias));
			}

			return builder.build();
		}

		private Map<String, String> parseParameterMapping(String content) {
			try {
				return new ParameterExpression(content);
			}
			catch (BuilderException e) {
				throw e;
			}
			catch (Exception e) {
				throw new BuilderException("Parsing error was found in mapping #{" + content + "}. Check syntax #{property|(expression), var1=value1, var2=value2, ...}.", e);
			}
		}
	}
}
