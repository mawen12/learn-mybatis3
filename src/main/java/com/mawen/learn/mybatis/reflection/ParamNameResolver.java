package com.mawen.learn.mybatis.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import com.mawen.learn.mybatis.annotations.Param;
import com.mawen.learn.mybatis.reflection.invoker.Invoker;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class ParamNameResolver {

	public static final String GENERIC_NAME_PREFIX = "param";

	private final boolean useActualParamName;

	private final SortedMap<Integer, String> names;

	private boolean hasParamAnnotation;

	public ParamNameResolver(Configuration config, Method method) {
		this.useActualParamName = config.isUseAcutalParamName();

		final Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		int paramCount = paramAnnotations.length;

		SortedMap<Integer, String> map = new TreeMap<>();

		for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
			if (isSpecialParameter(paramTypes[paramIndex])) {
				continue;
			}

			String name = null;
			for (Annotation annotation : paramAnnotations[paramIndex]) {
				if (annotation instanceof Param) {
					hasParamAnnotation = true;
					name = ((Param) annotation).value();
					break;
				}
			}

			if (name == null) {
				if (useActualParamName) {
					name = getActualParamName(method, paramIndex);
				}

				if (name == null) {
					name = String.valueOf(map.size());
				}
			}

			map.put(paramIndex, name);
		}

		names = Collections.unmodifiableSortedMap(map);

	}

	private String getActualParamName(Method method, int paramIndex) {
		return ParamNameUtil.getParamNames(method).get(paramIndex);
	}

	private static boolean isSpecialParameter(Class<?> clazz) {
		return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
	}

	public String[] getNames() {
		return names.values().toArray(new String[0]);
	}

	public Object getNamedParams(Object[] args) {
		final int paramCount = names.size();
		if (args == null || paramCount == 0) {
			return null;
		}
		else if (!hasParamAnnotation && paramCount == 1) {
			Object value = args[names.firstKey()];
			return wrapToMapIfCollection(value, useActualParamName ? names.get(0) : null);
		}
		else {
			final Map<String, Object> param = new ParamMap<>();
			int i = 0;
			for (Map.Entry<Integer, String> entry : names.entrySet()) {
				param.put(entry.getValue(), args[entry.getKey()]);
				final String genericParamName = GENERIC_NAME_PREFIX + (i + 1);
				if (!names.equals(genericParamName)) {
					param.put(genericParamName, args[entry.getKey()]);
				}
				i++;
			}
			return param;
		}
	}

	public static Object wrapToMapIfCollection(Object object, String actualParamName) {
		if (object instanceof Collection) {
			ParamMap<Object> map = new ParamMap<>();
			map.put("collection", object);
			if (object instanceof List) {
				map.put("list", object);
			}
			Optional.ofNullable(actualParamName).ifPresent(name -> map.put(name, object));
			return map;
		}
		else if (object != null && object.getClass().isArray()) {
			ParamMap<Object> map = new ParamMap<>();
			map.put("array", object);
			Optional.ofNullable(actualParamName).isPresent(name -> map.put(name, object));
			return map;
		}
		else {
			return object;
		}
	}
}
