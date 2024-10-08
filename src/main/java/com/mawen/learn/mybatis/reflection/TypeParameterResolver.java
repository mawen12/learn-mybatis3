package com.mawen.learn.mybatis.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class TypeParameterResolver {

	public static Type resolveFieldType(Field field, Type srcType) {
		Type fieldType = field.getGenericType();
		Class<?> declaringClass = field.getDeclaringClass();
		return resolveType(fieldType, srcType, declaringClass);
	}

	public static Type resolveReturnType(Method method, Type srcType) {
		Type returnType = method.getGenericReturnType();
		Class<?> declaringClass = method.getDeclaringClass();
		return resolveType(returnType, srcType, declaringClass);
	}

	public static Type[] resolveParamTypes(Method method, Type srcType) {
		Type[] paramTypes = method.getGenericParameterTypes();
		Class<?> declaringClass = method.getDeclaringClass();
		Type[] result = new Type[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			result[i] = resolveType(paramTypes[i], srcType, declaringClass);
		}
		return result;
	}

	private TypeParameterResolver() {}

	private static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
		if (type instanceof TypeVariable) {
			return resolveTypeVar((TypeVariable<?>) type, srcType, declaringClass);
		}
		else if (type instanceof ParameterizedType) {
			return resolveParameterizedType((ParameterizedType) type, srcType, declaringClass);
		}
		else if (type instanceof GenericArrayType) {
			return resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);
		}
		else {
			return type;
		}
	}

	private static Type resolveGenericArrayType(GenericArrayType genericArrayType, Type srcType, Class<?> declaringClass) {
		Type componentType = genericArrayType.getGenericComponentType();
		Type resolveComponentType = null;

		if (componentType instanceof TypeVariable) {
			resolveComponentType = resolveTypeVar((TypeVariable<?>) componentType, srcType, declaringClass);
		}
		else if (componentType instanceof GenericArrayType) {
			resolveComponentType = resolveGenericArrayType((GenericArrayType) componentType, srcType, declaringClass);
		}
		else if (componentType instanceof ParameterizedType) {
			resolveComponentType = resolveParameterizedType((ParameterizedType) componentType, srcType, declaringClass);
		}

		if (resolveComponentType instanceof Class) {
			return Array.newInstance((Class<?>) resolveComponentType, 0).getClass();
		}
		else {
			return new GenericArrayTypeImpl(resolveComponentType);
		}
	}

	private static ParameterizedType resolveParameterizedType(ParameterizedType parameterizedType, Type srcType, Class<?> declaringClass) {
		Class<?> rawType = (Class<?>) parameterizedType.getRawType();
		Type[] typeArgs = parameterizedType.getActualTypeArguments();
		Type[] args = new Type[typeArgs.length];

		for (int i = 0; i < typeArgs.length; i++) {
			if (typeArgs[i] instanceof TypeVariable) {
				args[i] = resolveTypeVar((TypeVariable<?>) typeArgs[i], srcType, declaringClass);
			}
			else if (typeArgs[i] instanceof ParameterizedType) {
				args[i] = resolveParameterizedType((ParameterizedType) typeArgs[i], srcType, declaringClass);
			}
			else if (typeArgs[i] instanceof WildcardType) {
				args[i] = resolveWildcardType((WildcardType) typeArgs[i], srcType, declaringClass);
			}
			else {
				args[i] = typeArgs[i];
			}
		}

		return new ParameterizedTypeImpl(rawType, null, args);
	}

	private static Type resolveWildcardType(WildcardType wildcardType, Type srcType, Class<?> declaringClass) {
		Type[] lowerBounds = resolveWildcardTypeBounds(wildcardType.getLowerBounds(), srcType, declaringClass);
		Type[] upperBounds = resolveWildcardTypeBounds(wildcardType.getUpperBounds(), srcType, declaringClass);
		return new WildcardTypeImpl(lowerBounds, upperBounds);
	}

	private static Type[] resolveWildcardTypeBounds(Type[] bounds, Type srcType, Class<?> declaringClass) {
		Type[] result = new Type[bounds.length];
		for (int i = 0; i < bounds.length; i++) {
			if (bounds[i] instanceof TypeVariable) {
				result[i] = resolveTypeVar((TypeVariable<?>) bounds[i], srcType, declaringClass);
			}
			else if (bounds[i] instanceof ParameterizedType) {
				result[i] = resolveParameterizedType((ParameterizedType)bounds[i], srcType, declaringClass);
			}
			else if (bounds[i] instanceof WildcardType) {
				result[i] = resolveWildcardType((WildcardType)bounds[i], srcType, declaringClass);
			}
			else {
				result[i] = bounds[i];
			}
		}
		return result;
	}

	private static Type scanSuperTypes(TypeVariable<?> typeVar, Type srcType, Class<?> declaringClass, Class<?> clazz, Type superClass) {
		if (superClass instanceof ParameterizedType) {
			ParameterizedType parentAsType = (ParameterizedType) superClass;
			Class<?> parentAsClass = (Class<?>) parentAsType.getRawType();
			TypeVariable<? extends Class<?>>[] parentTypeVars = parentAsClass.getTypeParameters();

			if (srcType instanceof ParameterizedType) {
				parentAsType = translateParentTypeVars((ParameterizedType) srcType, clazz, parentAsType);
			}

			if (declaringClass == parentAsClass) {
				for (int i = 0; i < parentTypeVars.length; i++) {
					if (typeVar.equals(parentTypeVars[i])) {
						return parentAsType.getActualTypeArguments()[i];
					}
				}
			}

			if (declaringClass.isAssignableFrom(parentAsClass)) {
				return resolveTypeVar(typeVar, parentAsType, declaringClass);
			}
		}
		else if (superClass instanceof Class && declaringClass.isAssignableFrom((Class<?>) superClass)) {
			return resolveTypeVar(typeVar, superClass, declaringClass);
		}
		return null;
	}

	private static Type resolveTypeVar(TypeVariable<?> typeVar, Type srcType, Class<?> declaringClass) {
		Type result;
		Class<?> clazz;

		if (srcType instanceof Class) {
			clazz = (Class<?>) srcType;
		}
		else if (srcType instanceof ParameterizedType) {
			clazz = (Class<?>) ((ParameterizedType) srcType).getRawType();
		}
		else {
			throw new IllegalArgumentException("The 2nd arg must be Class or ParameterizedType, but was: " + srcType.getClass());
		}

		if (clazz == declaringClass) {
			Type[] bounds = typeVar.getBounds();
			if (bounds.length > 0) {
				return bounds[0];
			}
			else {
				return Object.class;
			}
		}

		Type superclass = clazz.getGenericSuperclass();
		result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superclass);
		if (result != null) {
			return result;
		}

		Type[] superInterfaces = clazz.getGenericInterfaces();
		for (Type superInterface : superInterfaces) {
			result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superInterface);
			if (result != null) {
				return result;
			}
		}

		return Object.class;
	}

	private static ParameterizedType translateParentTypeVars(ParameterizedType srcType, Class<?> srcClass, ParameterizedType parentType) {
		Type[] parentTypeArgs = parentType.getActualTypeArguments();
		Type[] srcTypeArgs = srcType.getActualTypeArguments();
		TypeVariable<? extends Class<?>>[] srcTypeVars = srcClass.getTypeParameters();
		Type[] newParentArgs = new Type[parentTypeArgs.length];
		boolean noChange = true;

		for (int i = 0; i < parentTypeArgs.length; i++) {
			if (parentTypeArgs[i] instanceof TypeVariable) {
				for (int j = 0; j < srcTypeArgs.length; j++) {
					if (srcTypeArgs[j].equals(parentTypeArgs[i])) {
						noChange = false;
						newParentArgs[i] = srcTypeArgs[j];
					}
				}
			}
			else {
				newParentArgs[i] = parentTypeArgs[i];
			}
		}

		return noChange ? parentType : new ParameterizedTypeImpl((Class<?>) parentType.getRawType(), null, newParentArgs);
	}

	static class ParameterizedTypeImpl implements ParameterizedType {

		private Class<?> rawType;

		private Type ownerType;

		private Type[] actualTypeArguments;

		public ParameterizedTypeImpl(Class<?> rawType, Type ownerType, Type[] actualTypeArguments) {
			this.rawType = rawType;
			this.ownerType = ownerType;
			this.actualTypeArguments = actualTypeArguments;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return actualTypeArguments;
		}

		@Override
		public Type getRawType() {
			return rawType;
		}

		@Override
		public Type getOwnerType() {
			return ownerType;
		}
	}

	static class WildcardTypeImpl implements WildcardType {

		private Type[] lowerBounds;

		private Type[] upperBounds;

		public WildcardTypeImpl(Type[] lowerBounds, Type[] upperBounds) {
			this.lowerBounds = lowerBounds;
			this.upperBounds = upperBounds;
		}

		@Override
		public Type[] getUpperBounds() {
			return upperBounds;
		}

		@Override
		public Type[] getLowerBounds() {
			return lowerBounds;
		}
	}

	static class GenericArrayTypeImpl implements GenericArrayType {

		private Type genericComponentType;

		public GenericArrayTypeImpl(Type genericComponentType) {
			super();
			this.genericComponentType = genericComponentType;
		}

		@Override
		public Type getGenericComponentType() {
			return genericComponentType;
		}
	}
}
