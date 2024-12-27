package com.mawen.learn.mybatis.cache.impl;

import java.util.HashMap;
import java.util.Map;

import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.cache.CacheException;

/**
 * 类型别名为：PERPETUAL，默认缓存。
 * 基于HashMap的缓存实现。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/12
 */
public class PerpetualCache implements Cache {

	private final String id;

	private final Map<Object, Object> cache = new HashMap<>();

	public PerpetualCache(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void putObject(Object key, Object value) {
		cache.put(key, value);
	}

	@Override
	public Object getObject(Object key) {
		return cache.get(key);
	}

	@Override
	public Object removeObject(Object key) {
		return cache.remove(key);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public int getSize() {
		return cache.size();
	}

	@Override
	public int hashCode() {
		if (getId() == null) {
			throw new CacheException("Cache instance requires an ID.");
		}

		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (getId() == null) {
			throw new CacheException("Cache instance requires an ID.");
		}
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Cache)) {
			return false;
		}

		Cache other = (Cache) obj;
		return getId().equals(other.getId());
	}
}
