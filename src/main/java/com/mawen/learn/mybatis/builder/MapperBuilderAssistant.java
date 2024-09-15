package com.mawen.learn.mybatis.builder;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.executor.ErrorContext;
import com.mawen.learn.mybatis.mapping.CacheBuilder;
import com.mawen.learn.mybatis.mapping.ResultMapping;
import com.mawen.learn.mybatis.reflection.MetaClass;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.type.JdbcType;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class MapperBuilderAssistant extends BaseBuilder {

	private String currentNamespace;
	private final String resource;
	private Cache currentCache;
	private boolean unresolvedCacheRef;

	public MapperBuilderAssistant(Configuration configuration, String resource) {
		super(configuration);
		ErrorContext.instance().resource(resource);
		this.resource = resource;
	}

	public String getCurrentNamespace() {
		return currentNamespace;
	}

	public void setCurrentNamespace(String currentNamespace) {
		if (currentNamespace == null) {
			throw new BuilderException("The mapper element requires a namespace attribute to be specified.");
		}

		if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
			throw new BuilderException("Wrong namespace. Expected '" + this.currentNamespace + "' but found '" + currentNamespace + "'.");
		}

		this.currentNamespace = currentNamespace;
	}

	public String applyCurrentNamespace(String base, boolean isReference) {
		if (base == null) {
			return null;
		}

		if (isReference) {
			if (base.contains(".")) {
				return base;
			}
		}
		else {
			if (base.startsWith(currentNamespace + ".")) {
				return base;
			}
			if (base.contains(".")) {
				throw new BuilderException("Dots are not allowed in element names, please remove it from " + base);
			}
		}

		return currentNamespace + "." + base;
	}

	public Cache useCacheRef(String namespace) {
		if (namespace == null) {
			throw new BuilderException("cache-ref element requires a namespace attribute.");
		}

		try {
			unresolvedCacheRef = true;
			Cache cache = configuration.getCache(namespace);
			if (cache == null) {
				throw new IncompleteElementException("No cache fro namespace '" + namespace + "' could be found.");
			}

			currentCache = cache;
			unresolvedCacheRef = false;
			return cache;
		}
		catch (IllegalArgumentException e) {
			throw new IncompleteElementException();
		}
	}

	public Cache useNewCache(Class<? extends Cache> typeClass, Class<? extends Cache> evictionClass,
	                         Long flushInterval, Integer size, boolean readWrite, boolean blocking, Properties props) {
		new CacheBuilder(currentNamespace)
				.implementation();
	}



	private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
		if (javaType == null && property != null) {
			try {
				MetaClass metaResultType = MetaClass.forClass(resultType, configuration.getReflectorFactory());
				javaType = metaResultType.getSetterType(property);
			}
			catch (Exception ignored) {

			}
		}

		if (javaType == null) {
			javaType = Object.class;
		}

		return javaType;

	}

	private Class<?> resolveParameterJavaType(Class<?> resultType, String property, Class<?> javaType, JdbcType jdbcType) {
		if (javaType == null) {
			if (JdbcType.CURSOR.equals(jdbcType)) {
				javaType = ResultSet.class;
			}
			else if (Map.class.isAssignableFrom(resultType)) {
				javaType = Object.class;
			}
			else {
				MetaClass metaResultType = MetaClass.forClass(resultType, configuration.getReflectorFactory());
				javaType = metaResultType.getGetterType(property);
			}

			if (javaType == null) {
				javaType = Object.class;
			}

			return javaType;
		}
	}
}
