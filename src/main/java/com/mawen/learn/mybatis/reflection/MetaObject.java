package com.mawen.learn.mybatis.reflection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.property.PropertyTokenizer;
import com.mawen.learn.mybatis.reflection.wrapper.BeanWrapper;
import com.mawen.learn.mybatis.reflection.wrapper.CollectionWrapper;
import com.mawen.learn.mybatis.reflection.wrapper.MapWrapper;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapper;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class MetaObject {

	private final Object originalObject;
	private final ObjectWrapper objectWrapper;
	private final ObjectFactory objectFactory;
	private final ObjectWrapperFactory objectWrapperFactory;
	private final ReflectorFactory reflectorFactory;

	public MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
		this.originalObject = object;
		this.objectFactory = objectFactory;
		this.objectWrapperFactory = objectWrapperFactory;
		this.reflectorFactory = reflectorFactory;


		if (object instanceof ObjectWrapper) {
			this.objectWrapper = (ObjectWrapper) object;
		}
		else if (objectWrapperFactory.hasWrapperFor(object)) {
			this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
		}
		else if (object instanceof Map) {
			this.objectWrapper = new MapWrapper(this, (Map) object);
		}
		else if (object instanceof Collection) {
			this.objectWrapper = new CollectionWrapper(this, (Collection) object);
		}
		else {
			this.objectWrapper = new BeanWrapper(this, object);
		}
	}

	public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
		if (object == null) {
			return SystemMetaObject.NULL_META_OBJECT;
		}
		else {
			return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
		}
	}

	public Object getOriginalObject() {
		return originalObject;
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	public ObjectWrapperFactory getObjectWrapperFactory() {
		return objectWrapperFactory;
	}

	public ReflectorFactory getReflectorFactory() {
		return reflectorFactory;
	}

	public String findProperty(String propName, boolean useCamelCaseMapping) {
		return objectWrapper.findProperty(propName, useCamelCaseMapping);
	}

	public String[] getGetterNames() {
		return objectWrapper.getGetterNames();
	}

	public String[] getSetterNames() {
		return objectWrapper.getSetterNames();
	}

	public Class<?> getSetterType(String name) {
		return objectWrapper.getSetterType(name);
	}

	public Class<?> getGetterType(String name) {
		return objectWrapper.getGetterType(name);
	}

	public boolean hasSetter(String name) {
		return objectWrapper.hasSetter(name);
	}

	public boolean hasGetter(String name) {
		return objectWrapper.hasGetter(name);
	}

	public Object getValue(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
			if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
				return null;
			}
			else {
				return metaValue.getValue(prop.getChildren());
			}
		}
		else {
			return objectWrapper.get(prop);
		}
	}

	public void setValue(String name, Object value) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
			if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
				if (value == null) {
					return;
				}
				else {
					metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
				}
			}
			metaValue.setValue(prop.getChildren(), value);
		}
		else {
			objectWrapper.set(prop, value);
		}
	}

	public MetaObject metaObjectForProperty(String name) {
		Object value = getValue(name);
		return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
	}

	public ObjectWrapper getObjectWrapper() {
		return objectWrapper;
	}

	public boolean isCollection() {
		return objectWrapper.isCollection();
	}

	public void add(Object element) {
		objectWrapper.add(element);
	}

	public <E> void addAll(List<E> list) {
		objectWrapper.addAll(list);
	}
}
