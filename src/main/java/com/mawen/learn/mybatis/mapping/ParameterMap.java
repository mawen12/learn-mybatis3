package com.mawen.learn.mybatis.mapping;

import java.util.Collections;
import java.util.List;

import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/1
 */
public class ParameterMap {

	private String id;
	private Class<?> type;
	private List<ParameterMapping> parameterMappings;

	private ParameterMap() {}

	public static class Builder {

		private ParameterMap parameterMap = new ParameterMap();

		public Builder(Configuration configuration, String id, Class<?> type, List<ParameterMapping> parameterMappings) {
			parameterMap.id = id;
			parameterMap.type = type;
			parameterMap.parameterMappings = parameterMappings;
		}

		public Class<?> type() {
			return parameterMap.type;
		}

		public ParameterMap build() {
			parameterMap.parameterMappings = Collections.unmodifiableList(parameterMap.parameterMappings);
			return parameterMap;
		}
	}

	public String getId() {
		return id;
	}

	public Class<?> getType() {
		return type;
	}

	public List<ParameterMapping> getParameterMappings() {
		return parameterMappings;
	}
}
