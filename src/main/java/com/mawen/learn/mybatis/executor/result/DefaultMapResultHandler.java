package com.mawen.learn.mybatis.executor.result;

import java.util.Map;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.ReflectorFactory;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.mawen.learn.mybatis.session.ResultContext;
import com.mawen.learn.mybatis.session.ResultHandler;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class DefaultMapResultHandler<K, V> implements ResultHandler<V> {

	private final Map<K, V> mappedResults;
	private final String mapKey;
	private final ObjectFactory objectFactory;
	private final ObjectWrapperFactory objectWrapperFactory;
	private final ReflectorFactory reflectorFactory;

	public DefaultMapResultHandler(String mapKey, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
		this.mapKey = mapKey;
		this.objectFactory = objectFactory;
		this.objectWrapperFactory = objectWrapperFactory;
		this.reflectorFactory = reflectorFactory;
		this.mappedResults = objectFactory.create(Map.class);
	}

	@Override
	public void handleResult(ResultContext<? extends V> resultContext) {
		V value = resultContext.getResultObject();
		MetaObject metaValue = MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
		final K key = (K) metaValue.getValue(mapKey);
		mappedResults.put(key, value);
	}

	public Map<K, V> getMappedResults() {
		return mappedResults;
	}
}
