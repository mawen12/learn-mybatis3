package com.mawen.learn.mybatis.mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.mawen.learn.mybatis.annotations.Param;
import com.mawen.learn.mybatis.builder.BuilderException;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import com.mawen.learn.mybatis.reflection.ParamNameUtil;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/1
 */
public class ResultMap {

	private Configuration configuration;

	private String id;
	private Class<?> type;
	private List<ResultMapping> resultMappings;
	private List<ResultMapping> idResultMappings;
	private List<ResultMapping> constructorResultMappings;
	private List<ResultMapping> propertyResultMappings;
	private Set<String> mappedColumns;
	private Set<String> mappedProperties;
	private Discriminator discriminator;
	private boolean hasNestedResultMaps;
	private boolean hasNestedQueries;
	private Boolean autoMapping;

	private ResultMap() {
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public String getId() {
		return id;
	}

	public Class<?> getType() {
		return type;
	}

	public List<ResultMapping> getResultMappings() {
		return resultMappings;
	}

	public List<ResultMapping> getIdResultMappings() {
		return idResultMappings;
	}

	public List<ResultMapping> getConstructorResultMappings() {
		return constructorResultMappings;
	}

	public List<ResultMapping> getPropertyResultMappings() {
		return propertyResultMappings;
	}

	public Set<String> getMappedColumns() {
		return mappedColumns;
	}

	public Set<String> getMappedProperties() {
		return mappedProperties;
	}

	public Discriminator getDiscriminator() {
		return discriminator;
	}

	public boolean hasNestedResultMaps() {
		return hasNestedResultMaps;
	}

	public boolean hasNestedQueries() {
		return hasNestedQueries;
	}

	public Boolean getAutoMapping() {
		return autoMapping;
	}

	public void forceNestedResultMaps() {
		this.hasNestedResultMaps = true;
	}

	public static class Builder {

		private static final Log log = LogFactory.getLog(Builder.class);

		private final ResultMap resultMap = new ResultMap();

		public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings) {
			this(configuration, id, type, resultMappings, null);
		}

		public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings, Boolean autoMapping) {
			resultMap.configuration = configuration;
			resultMap.id = id;
			resultMap.type = type;
			resultMap.resultMappings = resultMappings;
			resultMap.autoMapping = autoMapping;
		}

		public Class<?> type() {
			return resultMap.type;
		}

		public Builder discriminator(Discriminator discriminator) {
			resultMap.discriminator = discriminator;
			return this;
		}

		public ResultMap build() {
			if (resultMap.id == null) {
				throw new IllegalArgumentException("ResultMaps must have an id");
			}

			resultMap.mappedColumns = new HashSet<>();
			resultMap.mappedProperties = new HashSet<>();
			resultMap.idResultMappings = new ArrayList<>();
			resultMap.constructorResultMappings = new ArrayList<>();
			resultMap.propertyResultMappings = new ArrayList<>();

			final List<String> constructorArgNames = new ArrayList<>();
			for (ResultMapping resultMapping : resultMap.resultMappings) {
				resultMap.hasNestedQueries = resultMap.hasNestedQueries || resultMapping.getNestedQueryId() != null;
				resultMap.hasNestedResultMaps = resultMap.hasNestedResultMaps || (resultMapping.getNestedResultMapId() != null && resultMapping.getResultSet() == null);

				final String column = resultMapping.getColumn();
				if (column != null) {
					resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
				}
				else if (resultMapping.isCompositeResult()) {
					for (ResultMapping compositeResultMapping : resultMapping.getComposites()) {
						String compositeColumn = compositeResultMapping.getColumn();
						if (compositeColumn != null) {
							resultMap.mappedColumns.add(compositeColumn.toUpperCase(Locale.ENGLISH));
						}
					}
				}

				final String property = resultMapping.getProperty();
				if (property != null) {
					resultMap.mappedProperties.add(property);
				}

				if (resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR)) {
					resultMap.constructorResultMappings.add(resultMapping);
					if (resultMapping.getProperty() != null) {
						constructorArgNames.add(resultMapping.getProperty());
					}
				}
				else {
					resultMap.propertyResultMappings.add(resultMapping);
				}

				if (resultMapping.getFlags().contains(ResultFlag.ID)) {
					resultMap.idResultMappings.add(resultMapping);
				}
			}

			if (resultMap.idResultMappings.isEmpty()) {
				resultMap.idResultMappings.addAll(resultMap.resultMappings);
			}

			if (!constructorArgNames.isEmpty()) {
				final List<String> actualArgNames = argNamesOfMatchingConstructor(constructorArgNames);

				if (actualArgNames == null) {
					throw new BuilderException("Error in result map '" + resultMap.id + "'. Failed to find a constructor in '" + resultMap.getType().getName()
					                           + "' by arg names " + constructorArgNames + ". There might be more info in debug log.");
				}

				resultMap.constructorResultMappings.sort((o1, o2) -> {
					int paramIndx1 = actualArgNames.indexOf(o1.getProperty());
					int paramIndx2 = actualArgNames.indexOf(o2.getProperty());
					return paramIndx1 - paramIndx2;
				});
			}

			resultMap.resultMappings = Collections.unmodifiableList(resultMap.resultMappings);
			resultMap.idResultMappings = Collections.unmodifiableList(resultMap.idResultMappings);
			resultMap.constructorResultMappings = Collections.unmodifiableList(resultMap.constructorResultMappings);
			resultMap.propertyResultMappings = Collections.unmodifiableList(resultMap.propertyResultMappings);
			resultMap.mappedColumns = Collections.unmodifiableSet(resultMap.mappedColumns);

			return resultMap;
		}

		private List<String> argNamesOfMatchingConstructor(List<String> constructorArgNames) {
			Constructor<?>[] constructors = resultMap.type.getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] paramTypes = constructor.getParameterTypes();
				if (constructorArgNames.size() == paramTypes.length) {
					List<String> paramNames = getArgNames(constructor);
					if (constructorArgNames.containsAll(paramNames) && argTypesMatch(constructorArgNames, paramTypes, paramNames)) {
						return paramNames;
					}
				}
			}
			return null;
		}

		private boolean argTypesMatch(final List<String> constructorArgNames, Class<?>[] paramTypes, List<String> paramNames) {
			for (int i = 0; i < constructorArgNames.size(); i++) {
				Class<?> actualType = paramTypes[paramNames.indexOf(constructorArgNames.get(i))];
				Class<?> specifiedType = resultMap.constructorResultMappings.get(i).getJavaType();
				if (!actualType.equals(specifiedType)) {
					if (log.isDebugEnabled()) {
						log.debug("While building result map '" + resultMap.id
								+ "', found a constructor with arg names " + constructorArgNames
								+ ", but the type of '" + constructorArgNames.get(i)
								+ "' did not match. Specified: [" + specifiedType.getName() + "] Declared: ["
								+ actualType.getName() + "]");
					}
					return false;
				}
			}
			return true;
		}

		private List<String> getArgNames(Constructor<?> constructor) {
			List<String> paramNames = new ArrayList<>();
			List<String> actualParamNames = null;
			final Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
			int paramCount = paramAnnotations.length;

			for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
				String name = null;
				for (Annotation annotation : paramAnnotations[paramIndex]) {
					if (annotation instanceof Param) {
						name = ((Param) annotation).value();
						break;
					}
				}

				if (name == null && resultMap.configuration.isUseActualParamName()) {
					if (actualParamNames == null) {
						actualParamNames = ParamNameUtil.getParamNames(constructor);
					}
					if (actualParamNames.size() > paramIndex) {
						name = actualParamNames.get(paramIndex);
					}
				}

				paramNames.add(name != null ? name : "arg" + paramIndex);
			}

			return paramNames;
		}
	}
}
