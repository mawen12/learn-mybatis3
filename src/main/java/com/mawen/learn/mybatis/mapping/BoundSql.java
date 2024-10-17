package com.mawen.learn.mybatis.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.property.PropertyTokenizer;
import com.mawen.learn.mybatis.session.Configuration;
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public class BoundSql {

	@Getter
	private final String sql;
	@Getter
	private final List<ParameterMapping> parameterMappings;
	@Getter
	private final Object parameterObject;
	private final Map<String, Object> additionalParameters;
	private final MetaObject metaParameters;

	public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
		this.sql = sql;
		this.parameterMappings = parameterMappings;
		this.parameterObject = parameterObject;
		this.additionalParameters = new HashMap<>();
		this.metaParameters = configuration.newMetaObject(additionalParameters);
	}

	public boolean hasAdditionalParameter(String name) {
		String paramName = new PropertyTokenizer(name).getName();
		return additionalParameters.containsKey(paramName);
	}

	public void setAdditionalParameter(String name, Object value) {
		metaParameters.setValue(name, value);
	}

	public Object getAdditionalParameter(String name) {
		return metaParameters.getValue(name);
	}
}
