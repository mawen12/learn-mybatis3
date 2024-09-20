package com.mawen.learn.mybatis.executor;

import java.lang.reflect.Array;
import java.util.List;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/12
 */
public class ResultExtractor {

	private final Configuration configuration;
	private final ObjectFactory objectFactory;

	public ResultExtractor(Configuration configuration, ObjectFactory objectFactory) {
		this.configuration = configuration;
		this.objectFactory = objectFactory;
	}

	public Object extractObjectFromList(List<Object> list, Class<?> targetType) {
		Object value = null;
		if (targetType != null && targetType.isAssignableFrom(list.getClass())) {
			value = list;
		}
		else if (targetType != null && objectFactory.isCollection(targetType)) {
			value = objectFactory.create(targetType);
			MetaObject metaObject = configuration.newMetaObject(value);
			metaObject.addAll(list);
		}
		else if (targetType != null && targetType.isArray()) {
			Class<?> arrayComponentType = targetType.getComponentType();
			Object array = Array.newInstance(arrayComponentType, list.size());
			if (arrayComponentType.isPrimitive()) {
				for (int i = 0; i < list.size(); i++) {
					Array.set(array, i, list.get(i));
				}
				value = array;
			}
			else {
				value = list.toArray((Object[]) array);
			}
		}
		else {
			if (list != null && list.size() > 1) {
				throw new ExecutorException("Statement returned more than one row, where no more than one was expected.");
			}
			else if (list != null && list.size() == 1) {
				value = list.get(0);
			}
		}
		return value;
	}
}
