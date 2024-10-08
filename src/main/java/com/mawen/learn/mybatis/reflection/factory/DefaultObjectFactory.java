package com.mawen.learn.mybatis.reflection.factory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.mawen.learn.mybatis.reflection.ReflectionException;
import com.mawen.learn.mybatis.reflection.Reflector;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class DefaultObjectFactory implements ObjectFactory, Serializable {

	private static final long serialVersionUID = 1706367531889743852L;

	@Override
	public <T> T create(Class<T> type) {
		return create(type, null, null);
	}

	@Override
	public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		Class<?> classToCreate = resolveInterface(type);
		return (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
	}

	@Override
	public <T> boolean isCollection(Class<T> type) {
		return Collection.class.isAssignableFrom(type);
	}

	protected Class<?> resolveInterface(Class<?> type) {
		Class<?> classToCreate;

		if (type == List.class || type == Collection.class || type == Iterator.class) {
			classToCreate = ArrayList.class;
		}
		else if (type == Map.class) {
			classToCreate = HashMap.class;
		}
		else if (type == SortedSet.class) {
			classToCreate = TreeSet.class;
		}
		else if (type == Set.class) {
			classToCreate = HashSet.class;
		}
		else {
			classToCreate = type;
		}

		return classToCreate;
	}

	private <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		try {
			Constructor<T> constructor;
			if (constructorArgTypes == null || constructorArgs == null) {
				constructor = type.getDeclaredConstructor();
				try {
					return constructor.newInstance();
				}
				catch (IllegalAccessException e) {
					if (Reflector.canControlMemberAccessible()) {
						constructor.setAccessible(true);
						return constructor.newInstance();
					}
					else {
						throw e;
					}
				}
			}

			constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[0]));
			try {
				return constructor.newInstance(constructorArgs.toArray(new Object[0]));
			}
			catch (IllegalAccessException e) {
				if (Reflector.canControlMemberAccessible()) {
					constructor.setAccessible(true);
					return constructor.newInstance(constructorArgs.toArray(new Object[0]));
				}
				else {
					throw e;
				}
			}

		}
		catch (Exception e) {
			String argTypes = Optional.ofNullable(constructorArgTypes)
					.orElseGet(Collections::emptyList)
					.stream().map(Class::getSimpleName)
					.collect(Collectors.joining(","));

			String argValues = Optional.ofNullable(constructorArgs)
					.orElseGet(Collections::emptyList)
					.stream().map(String::valueOf)
					.collect(Collectors.joining(","));

			throw new ReflectionException("Error instantiating " + type + " with invalid types (" + argTypes + ") or values (" + argValues + "). Cause: " + e);
		}
	}
}
