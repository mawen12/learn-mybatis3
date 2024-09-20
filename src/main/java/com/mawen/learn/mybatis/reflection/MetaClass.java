package com.mawen.learn.mybatis.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import com.mawen.learn.mybatis.reflection.invoker.GetFieldInvoker;
import com.mawen.learn.mybatis.reflection.invoker.Invoker;
import com.mawen.learn.mybatis.reflection.invoker.MethodInvoker;
import com.mawen.learn.mybatis.reflection.property.PropertyTokenizer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class MetaClass {

	private final ReflectorFactory reflectorFactory;
	private final Reflector reflector;

	public MetaClass(Class<?> type, ReflectorFactory reflectorFactory) {
		this.reflectorFactory = reflectorFactory;
		this.reflector = reflectorFactory.findForClass(type);
	}

	public static MetaClass forClass(Class<?> type, ReflectorFactory reflectorFactory) {
		return new MetaClass(type, reflectorFactory);
	}

	public MetaClass metaClassForProperty(String name) {
		Class<?> propType = reflector.getGetterType(name);
		return MetaClass.forClass(propType, reflectorFactory);
	}

	public String findProperty(String name) {
		StringBuilder prop = buildProperty(name, new StringBuilder());
		return prop.length() > 0 ? prop.toString() : null;
	}

	public String findProperty(String name, boolean useCamelCaseMapping) {
		if (useCamelCaseMapping) {
			name = name.replace("_", "");
		}
		return findProperty(name);
	}

	public String[] getGetterNames() {
		return reflector.getGetablePropertyNames();
	}

	public String[] getSetterNames() {
		return reflector.getSetablePropertyNames();
	}

	public Class<?> getSetterType(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			MetaClass metaProp = metaClassForProperty(prop);
			return metaProp.getSetterType(prop.getChildren());
		}

		return getGetterType(prop);
	}

	public Class<?> getGetterType(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			MetaClass metaProp = metaClassForProperty(name);
			return metaProp.getGetterType(prop.getChildren());
		}

		return getGetterType(name);
	}

	private MetaClass metaClassForProperty(PropertyTokenizer prop) {
		Class<?> propType = getGetterType(prop);
		return MetaClass.forClass(propType, reflectorFactory);
	}

	private Class<?> getGetterType(PropertyTokenizer prop) {
		Class<?> type = reflector.getGetterType(prop.getName());
		if (prop.getIndexedName() != null && Collection.class.isAssignableFrom(type)) {
			Type returnType = getGenericGetterType(prop.getName());
			if (returnType instanceof ParameterizedType) {
				Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
				if (actualTypeArguments != null && actualTypeArguments.length == 1) {
					returnType = actualTypeArguments[0];
					if (returnType instanceof Class) {
						type = (Class<?>) returnType;
					}
					else if (returnType instanceof ParameterizedType) {
						type = (Class<?>) ((ParameterizedType) returnType).getRawType();
					}
				}
			}
		}
		return type;
	}

	private Type getGenericGetterType(String propName) {
		try {
			Invoker invoker = reflector.getGetInvoker(propName);
			if (invoker instanceof MethodInvoker) {
				Field declaredField = MethodInvoker.class.getDeclaredField("method");
				declaredField.setAccessible(true);
				Method method = (Method) declaredField.get(invoker);
				return TypeParameterResolver.resolveReturnType(method, reflector.getType());
			}
			else if (invoker instanceof GetFieldInvoker) {
				Field declaredField = GetFieldInvoker.class.getDeclaredField("field");
				declaredField.setAccessible(true);
				Field field = (Field) declaredField.get(invoker);
				return TypeParameterResolver.resolveFieldType(field, reflector.getType());
			}
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			// Ignored
		}
		return null;
	}

	public boolean hasSetter(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			if (reflector.hasSetter(prop.getName())) {
				MetaClass metaProp = metaClassForProperty(prop.getName());
				return metaProp.hasSetter(prop.getChildren());
			}
			else {
				return false;
			}
		}
		else {
			return reflector.hasSetter(prop.getName());
		}
	}

	public boolean hasGetter(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			if (reflector.hasGetter(prop.getName())) {
				MetaClass metaProp = metaClassForProperty(prop.getName());
				return metaProp.hasSetter(prop.getChildren());
			}
			else {
				return false;
			}
		}
		else {
			return reflector.hasGetter(prop.getName());
		}
	}

	public Invoker getGetInvoker(String name) {
		return reflector.getGetInvoker(name);
	}

	public Invoker getSetInvoker(String name) {
		return reflector.getSetInvoker(name);
	}

	private StringBuilder buildProperty(String name, StringBuilder builder) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			String propertyName = reflector.findPropertyName(prop.getName());
			if (propertyName != null) {
				builder.append(propertyName);
				builder.append(".");
				MetaClass metaProp = metaClassForProperty(propertyName);
				metaProp.buildProperty(prop.getChildren(), builder);
			}
		}
		else {
			String propertyName = reflector.findPropertyName(name);
			if (propertyName != null) {
				builder.append(propertyName);
			}
		}
		return builder;
	}

	public boolean hasDefaultConstructor() {
		return reflector.hasDefaultConstructor();
	}
}
