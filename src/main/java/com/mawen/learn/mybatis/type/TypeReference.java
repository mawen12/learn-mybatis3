package com.mawen.learn.mybatis.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public abstract class TypeReference<T> {

	private final Type rawType;

	protected TypeReference() {
		this.rawType = getSuperClassTypeParameter(getClass());
	}

	Type getSuperClassTypeParameter(Class<?> clazz) {
		Type genericSuperclass = clazz.getGenericSuperclass();
		if (genericSuperclass instanceof Class) {
			if (TypeReference.class != genericSuperclass) {
				return getSuperClassTypeParameter(clazz.getSuperclass());
			}

			throw new TypeException("'" + getClass() + "' extends TypeReference but misses the type parameter. " +
			                        "Remove the extension or add a type parameter to it.");
		}

		Type rawType = ((ParameterizedType)genericSuperclass).getActualTypeArguments()[0];
		if (rawType instanceof ParameterizedType) {
			rawType = ((ParameterizedType) rawType).getRawType();
		}

		return rawType;
	}

	public Type getRawType() {
		return rawType;
	}

	@Override
	public String toString() {
		return rawType.toString();
	}
}
