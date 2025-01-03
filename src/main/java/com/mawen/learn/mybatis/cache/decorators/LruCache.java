package com.mawen.learn.mybatis.cache.decorators;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mawen.learn.mybatis.cache.Cache;

/**
 * 最近最少使用算法。类型别名为：LRU。装饰器设计模式，责任链设计模式。
 * 基于LinkedHashMap实现的缓存，最大支持1024个缓存对象。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class LruCache implements Cache {

	private final Cache delegate;
	private Map<Object, Object> keyMap;
	private Object eldestKey;

	public LruCache(Cache delegate) {
		this.delegate = delegate;
		setSize(1024);
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public void putObject(Object key, Object value) {
		delegate.putObject(key, value);
		cycleKeyList(key);
	}

	@Override
	public Object getObject(Object key) {
		keyMap.get(key);
		return delegate.getObject(key);
	}

	@Override
	public Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		delegate.clear();
		keyMap.clear();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	public void setSize(final int size) {
		keyMap = new LinkedHashMap<Object, Object>(size, 0.75F, true) {
			private static final long serialVersionUID = 4267176411845948333L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
				boolean tooBig = size() > size;
				if (tooBig) {
					eldestKey = eldest.getKey();
				}
				return tooBig;
			}
		};
	}

	private void cycleKeyList(Object key) {
		keyMap.put(key, key);
		if (eldestKey != null) {
			delegate.removeObject(eldestKey);
			eldestKey = null;
		}
	}
}
