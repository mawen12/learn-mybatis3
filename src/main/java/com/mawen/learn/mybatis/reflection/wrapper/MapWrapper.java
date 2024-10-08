package com.mawen.learn.mybatis.reflection.wrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.SystemMetaObject;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.property.PropertyTokenizer;


/**
 * 对 {@link Map} 及其子类访问的封装，通过 PropertyTokenizer 实现键的多样性扩展，支持两种访问模式：
 * <ul>
 *     <li>简单类型的值：</li>
 *     <pre>{@code
 *     	this.mapWrapper.set(new PropertyTokenizer("key"), "mybatis");
 *
 *     	Object value = this.mapWrapper.get(new PropertyTokenizer("key"));
 *
 *     	assertEquals("mybatis", value);
 *     }</pre>
 *     <li>集合和数组类型的值，基于索引访问：</li>
 *     <pre>{@code
 *     	this.mapWrapper.set(new PropertyTokenizer("key"), Arrays.asList(1L, 2L));
 *
 *     	Object value = this.mapWrapper.get(new PropertyTokenizer("key[0]"));
 *
 *     	assertEquals(1L, value);
 *     }</pre>
 * </ul>
 *
 * <p>需要注意的是，不支持 person.id 这种嵌套访问。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class MapWrapper extends BaseWrapper {

	private final Map<String, Object> map;

	public MapWrapper(MetaObject metaObject, Map<String, Object> map) {
		super(metaObject);
		this.map = map;
	}

	@Override
	public Object get(PropertyTokenizer prop) {
		if (prop.hasNext()) {
			return getChildValue(prop);
		}
		else if (prop.getIndex() != null) {
			return getCollectionValue(prop, resolveCollection(prop, map));
		}
		else {
			return map.get(prop.getName());
		}
	}

	@Override
	public void set(PropertyTokenizer prop, Object value) {
		if (prop.hasNext()) {
			setChildValue(prop, value);
		}
		else if (prop.getIndex() != null) {
			setCollectionValue(prop, resolveCollection(prop, map), value);
		}
		else {
			map.put(prop.getName(), value);
		}
	}

	@Override
	public String findProperty(String name, boolean useCamelCaseMapping) {
		return name;
	}

	@Override
	public String[] getGetterNames() {
		return map.keySet().toArray(new String[0]);
	}

	@Override
	public String[] getSetterNames() {
		return map.keySet().toArray(new String[0]);
	}

	@Override
	public Class<?> getSetterType(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
			if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
				return Object.class;
			}
			else {
				return metaValue.getSetterType(prop.getChildren());
			}
		}
		else {
			if (map.get(name) != null) {
				return map.get(name).getClass();
			}
			else {
				return Object.class;
			}
		}
	}

	@Override
	public Class<?> getGetterType(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
			if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
				return Object.class;
			}
			else {
				return metaValue.getGetterType(prop.getChildren());
			}
		}
		else {
			if (map.get(name) != null) {
				return map.get(name).getClass();
			}
			else {
				return Object.class;
			}
		}
	}

	@Override
	public boolean hasSetter(String name) {
		return true;
	}

	@Override
	public boolean hasGetter(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			if (map.containsKey(prop.getIndexedName())) {
				MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
				if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
					return true;
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
			return map.containsKey(prop.getName());
		}
	}

	@Override
	public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
		Map<String, Object> map = new HashMap<>();
		set(prop, map);
		return MetaObject.forObject(map, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory(), metaObject.getReflectorFactory());
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

	protected Object getChildValue(PropertyTokenizer prop) {
		MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
		if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
			return null;
		}
		return metaValue.getValue(prop.getChildren());
	}

	protected void setChildValue(PropertyTokenizer prop, Object value) {
		MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
		if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
			if (value == null) {
				return;
			}

			metaValue = instantiatePropertyValue(null, new PropertyTokenizer(prop.getName()), metaValue.getObjectFactory());
		}
		metaValue.setValue(prop.getChildren(), value);
	}
}
