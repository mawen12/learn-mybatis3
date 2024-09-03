package com.mawen.learn.mybatis.reflection.wrapper;

import java.util.List;

import com.mawen.learn.mybatis.reflection.ExceptionUtil;
import com.mawen.learn.mybatis.reflection.MetaClass;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.ReflectionException;
import com.mawen.learn.mybatis.reflection.SystemMetaObject;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.invoker.Invoker;
import com.mawen.learn.mybatis.reflection.property.PropertyTokenizer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class BeanWrapper extends BaseWrapper {

	private final Object object;
	private final MetaClass metaClass;

	public BeanWrapper(MetaObject metaObject, Object object) {
		super(metaObject);
		this.object = object;
		this.metaClass = MetaClass.forClass(object.getClass(), metaObject.getReflectorFactory());
	}

	@Override
	public Object get(PropertyTokenizer prop) {
		if (prop.getIndex() != null) {
			Object collection = resolveCollection(prop, object);
			return getCollectionValue(prop, collection);
		}
		else {
			return getBeanProperty(prop, object);
		}
	}

	@Override
	public void set(PropertyTokenizer prop, Object value) {
		if (prop.getIndex() != null) {
			Object collection = resolveCollection(prop, object);
			setCollectionValue(prop, collection, value);
		}
		else {
			setBeanProperty(prop, object, value);
		}
	}

	@Override
	public String findProperty(String name, boolean useCamelCaseMapping) {
		return metaClass.findProperty(name, useCamelCaseMapping);
	}

	@Override
	public String[] getGetterNames() {
		return metaClass.getGetterNames();
	}

	@Override
	public String[] getSetterNames() {
		return metaClass.getSetterNames();
	}

	@Override
	public Class<?> getSetterType(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
			if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
				return metaClass.getSetterType(name);
			}
			else {
				return metaValue.getSetterType(prop.getChildren());
			}
		}
		else {
			return metaClass.getSetterType(name);
		}
	}

	@Override
	public Class<?> getGetterType(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
			if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
				return metaClass.getGetterType(name);
			}
			else {
				return metaValue.getGetterType(prop.getChildren());
			}
		}
		else {
			return metaClass.getGetterType(name);
		}
	}

	@Override
	public boolean hasSetter(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			if (metaClass.hasSetter(prop.getIndexedName())) {
				MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
				if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
					return metaClass.hasSetter(name);
				}
				else {
					return metaValue.hasSetter(prop.getChildren());
				}
			}
			else {
				return false;
			}
		}
		else {
			return metaClass.hasSetter(name);
		}
	}

	@Override
	public boolean hasGetter(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			if (metaClass.hasGetter(prop.getIndexedName())) {
				MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
				if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
					return metaClass.hasGetter(name);
				}
				else {
					return metaValue.hasGetter(prop.getChildren());
				}
			}
			else {
				return false;
			}
		}
		else {
			return metaClass.hasGetter(name);
		}
	}

	@Override
	public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
		MetaObject metaValue;
		Class<?> type = getSetterType(prop.getName());
		try {
			Object newObject = objectFactory.create(type);
			metaValue = MetaObject.forObject(newObject, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory(), metaObject.getReflectorFactory());
			set(prop, newObject);
		}
		catch (Exception e) {
			throw new ReflectionException("Cannot set value of property '" + name + "' because '" + name + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause: " + e.toString(), e);
		}

		return metaValue;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public void add(Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <E> void addAll(List<E> element) {
		throw new UnsupportedOperationException();
	}

	private Object getBeanProperty(PropertyTokenizer prop, Object object) {
		try {
			Invoker method = metaClass.getGetInvoker(prop.getName());
			try {
				return method.invoke(object, NO_ARGUMENTS);
			}
			catch (Throwable e) {
				throw ExceptionUtil.unwrapThrowable(e);
			}
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Throwable t) {
			throw new ReflectionException("Could not get property '" + prop.getName() + "' from " + object.getClass() + ". Cause: " + t.toString(), t);
		}
	}

	private void setBeanProperty(PropertyTokenizer prop, Object object, Object value) {
		try {
			Invoker method = metaClass.getSetInvoker(prop.getName());
			Object[] params = {value};
			try {
				method.invoke(object, params);
			}
			catch (Throwable e) {
				throw ExceptionUtil.unwrapThrowable(e);
			}
		}
		catch (Throwable e) {
			throw new ReflectionException("Could not set property '" + prop.getName() + "' of " + object.getClass() + "' with value '" + value + "'. Cause: " + e.toString(), e);
		}
	}
}
