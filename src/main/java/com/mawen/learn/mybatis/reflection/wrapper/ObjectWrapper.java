package com.mawen.learn.mybatis.reflection.wrapper;

import java.util.List;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.property.PropertyTokenizer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public interface ObjectWrapper {

	Object get(PropertyTokenizer prop);

	void set(PropertyTokenizer prop, Object value);

	String findProperty(String name, boolean useCamelCaseMapping);

	String[] getGetterNames();

	String[] getSetterNames();

	Class<?> getSetterType(String name);

	Class<?> getGetterType(String name);

	boolean hasSetter(String name);

	boolean hasGetter(String name);

	MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

	boolean isCollection();

	void add(Object element);

	<E> void addAll(List<E> element);
}
