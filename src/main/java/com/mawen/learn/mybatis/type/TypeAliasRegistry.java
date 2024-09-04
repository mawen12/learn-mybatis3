package com.mawen.learn.mybatis.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mawen.learn.mybatis.io.ResolverUtil;
import com.mawen.learn.mybatis.io.Resources;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class TypeAliasRegistry {

	private final Map<String, Class<?>> typeAliases = new HashMap<>();

	public TypeAliasRegistry() {
		registerAlias("string", String.class);
		registerAlias("byte", Byte.class);
		registerAlias("short", Short.class);
		registerAlias("char", Character.class);
		registerAlias("character", Character.class);
		registerAlias("int", Integer.class);
		registerAlias("integer", Integer.class);
		registerAlias("long", Long.class);
		registerAlias("float", Float.class);
		registerAlias("double", Double.class);
		registerAlias("boolean", Boolean.class);

		registerAlias("byte[]", Byte[].class);
		registerAlias("short[]", Short[].class);
		registerAlias("char[]", Character[].class);
		registerAlias("character[]", Character[].class);
		registerAlias("short[]", Short[].class);
		registerAlias("int[]", Integer[].class);
		registerAlias("integer[]", Integer[].class);
		registerAlias("float[]", Float[].class);
		registerAlias("double[]", Double[].class);
		registerAlias("boolean[]", Boolean[].class);

		registerAlias("_byte", byte.class);
		registerAlias("_char", char.class);
		registerAlias("_character", char.class);
		registerAlias("_short", short.class);
		registerAlias("_int", int.class);
		registerAlias("_integer", int.class);
		registerAlias("_long", long.class);
		registerAlias("_float", float.class);
		registerAlias("_double", double.class);
		registerAlias("_boolean", boolean.class);

		registerAlias("_byte[]", byte[].class);
		registerAlias("_char[]", char[].class);
		registerAlias("_character[]", char[].class);
		registerAlias("_short[]", short[].class);
		registerAlias("_int[]", int[].class);
		registerAlias("_integer[]", int[].class);
		registerAlias("_long[]", long[].class);
		registerAlias("_float[]", float[].class);
		registerAlias("_double[]", double[].class);
		registerAlias("_boolean[]", boolean[].class);

		registerAlias("date", Date.class);
		registerAlias("decimal", BigDecimal.class);
		registerAlias("bigdecimal", BigDecimal.class);
		registerAlias("biginteger", BigInteger.class);
		registerAlias("object", Object.class);

		registerAlias("date[]", Date[].class);
		registerAlias("decimal[]", BigDecimal[].class);
		registerAlias("bigdecimal[]", BigDecimal[].class);
		registerAlias("biginteger[]", BigInteger[].class);
		registerAlias("object[]", Object[].class);

		registerAlias("map", Map.class);
		registerAlias("hashmap", HashMap.class);
		registerAlias("list", List.class);
		registerAlias("arrayList", ArrayList.class);
		registerAlias("collection", Collection.class);
		registerAlias("iterator", Iterator.class);

		registerAlias("ResultSet", ResultSet.class);

	}

	public <T> Class<T> resolveAlias(String alias) {
		try {
			if (alias == null) {
				return null;
			}

			String key = alias.toLowerCase(Locale.ENGLISH);
			Class<T> value;
			if (typeAliases.containsKey(key)) {
				value = (Class<T>) typeAliases.get(key);
			}
			else {
				value = (Class<T>) Resources.classForName(alias);
			}
			return value;
		}
		catch (ClassNotFoundException e) {
			throw new TypeException("Could not resolve type alias '" + alias + "'. Cause: " + e, e);
		}
	}

	public void registerAliases(String packageName) {
		registerAlias(packageName, Object.class);
	}

	public void registerAliases(String packageName, Class<?> superType) {
		ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
		resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
		Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();

		for (Class<? extends Class<?>> type : typeSet) {
			if (!type.isAnonymousClass() && !type.isInterface() && !type.isMemberClass()) {
				registerAlias(type);
			}
		}
	}

	public void registerAlias(Class<?> type) {
		String alias = type.getSimpleName();
		Alias annotation = type.getAnnotation(Alias.class);
		if (annotation != null) {
			alias = annotation.value();
		}
		registerAlias(alias, type);
	}

	public void registerAlias(String alias, Class<?> value) {
		if (alias == null) {
			throw new TypeException("The parameter alias cannot be null");
		}

		String key = alias.toLowerCase(Locale.ENGLISH);
		if (typeAliases.containsKey(key) && typeAliases.get(key) != null && !typeAliases.get(key).equals(value)) {
			throw new TypeException("The alias '" + alias + "' is already mapped to the value '" + typeAliases.get(key).getName() + "'.");
		}
		typeAliases.put(key, value);
	}

	public void registerAlias(String alias, String value) {
		try {
			registerAlias(alias, Resources.classForName(value));
		}
		catch (ClassNotFoundException e) {
			throw new TypeException("Error registering type alias " + alias + " for " + value + ". Cause: " + e, e);
		}
	}

	public Map<String, Class<?>> getTypeAliases() {
		return Collections.unmodifiableMap(typeAliases);
	}
}
