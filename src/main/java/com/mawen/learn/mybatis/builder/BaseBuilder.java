package com.mawen.learn.mybatis.builder;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.mawen.learn.mybatis.mapping.ParameterMode;
import com.mawen.learn.mybatis.mapping.ResultSetType;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.TypeAliasRegistry;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;
import sun.security.krb5.Config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public abstract class BaseBuilder {

	protected final Configuration configuration;
	protected final TypeAliasRegistry typeAliasRegistry;
	protected final TypeHandlerRegistry typeHandlerRegistry;

	public BaseBuilder(Configuration configuration) {
		this.configuration = configuration;
		this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
		this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	protected Pattern parseExpression(String regex, String defaultValue) {
		return Pattern.compile(regex == null ? defaultValue : regex);
	}

	protected Boolean booleanValueOf(String value, Boolean defaultValue) {
		return value == null ? defaultValue : Boolean.valueOf(value);
	}

	protected Integer integerValueOf(String value, Integer defaultValue) {
		return value == null ? defaultValue : Integer.valueOf(value);
	}

	protected Set<String> stringSetValueOf(String value, String defaultValue) {
		value = value == null ? defaultValue : value;
		return new HashSet<>(Arrays.asList(value.split(",")));
	}

	protected JdbcType resolveJdbcType(String alias) {
		if (alias == null) {
			return null;
		}

		try {
			return JdbcType.valueOf(alias);
		}
		catch (IllegalArgumentException e) {
			throw new BuilderException("Error resolving JdbcType. Cause: " + e, e);
		}
	}

	protected ResultSetType resolveResultSetType(String alias) {
		if (alias == null) {
			return null;
		}

		try {
			return ResultSetType.valueOf(alias);
		}
		catch (IllegalArgumentException e) {
			throw new BuilderException("Error resolving ResultSetType. Cause: " + e, e);
		}
	}

	protected ParameterMode resolveParameterMode(String alias) {
		if (alias == null) {
			return null;
		}

		try {
			return ParameterMode.valueOf(alias);
		}
		catch (IllegalArgumentException e) {
			throw new BuilderException("Error resolving ParameterMode. Cause: " + e, e);
		}
	}

	protected Object createInstance(String alias) {
		Class<?> clazz = resolveClass(alias);
		if (clazz == null) {
			return null;
		}

		try {
			return clazz.getDeclaredConstructor().newInstance();
		}
		catch (Exception e) {
			throw new BuilderException("Error creating instance. Cause: " + e, e);
		}
	}

	protected <T> Class<? extends T> resolveClass(String alias) {
		if (alias == null) {
			return null;
		}

		try {
			return resolveAlias(alias);
		}
		catch (Exception e) {
			throw new BuilderException("Error resolving Class. Cause: " + e, e);
		}
	}

	protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, String typeHandlerAlias) {
		if (typeHandlerAlias == null) {
			return null;
		}

		Class<?> type = resolveClass(typeHandlerAlias);
		if (type != null && !TypeHandler.class.isAssignableFrom(type)) {
			throw new BuilderException("Type " + type.getName() + " is not a valid TypeHandler because it does not implement TypeHandler interface.");
		}

		Class<? extends TypeHandler<?>> typeHandlerType = (Class<? extends TypeHandler<?>>) type;
		return resolveTypeHandler(typeHandlerType, typeHandlerType);
	}

	protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
		if (typeHandlerType == null) {
			return null;
		}

		TypeHandler<?> handler = typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
		if (handler == null) {
			handler = typeHandlerRegistry.getInstance(javaType, typeHandlerType);
		}
		return handler;
	}

	protected <T> Class<? extends T> resolveAlias(String alias) {
		return typeAliasRegistry.resolveAlias(alias);
	}
}
