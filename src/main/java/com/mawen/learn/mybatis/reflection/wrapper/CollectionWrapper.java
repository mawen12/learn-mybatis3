package com.mawen.learn.mybatis.reflection.wrapper;

import java.util.Collection;
import java.util.List;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.property.PropertyTokenizer;

/**
 * 对 {@link Collection} 及其子类访问的封装，仅支持 add/addAll 方法.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class CollectionWrapper implements ObjectWrapper{

	private final Collection<Object> object;

	public CollectionWrapper(MetaObject metaObject, Collection<Object> object) {
		this.object = object;
	}

	@Override
	public Object get(PropertyTokenizer prop) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(PropertyTokenizer prop, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String findProperty(String name, boolean useCamelCaseMapping) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getGetterNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getSetterNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<?> getSetterType(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<?> getGetterType(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasSetter(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasGetter(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCollection() {
		return true;
	}

	@Override
	public void add(Object element) {
		object.add(element);
	}

	@Override
	public <E> void addAll(List<E> element) {
		object.addAll(element);
	}
}
