package com.mawen.learn.mybatis.cache;

import java.util.HashMap;
import java.util.Map;

import com.mawen.learn.mybatis.cache.decorators.TransactionalCache;
import com.mawen.learn.mybatis.util.MapUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class TransactionCacheManager {

	private final Map<Cache, TransactionalCache> transactionCaches = new HashMap<>();

	public void clear(Cache cache) {
		getTransactionalCache(cache).clear();
	}

	public Object getObject(Cache cache, CacheKey key) {
		return getTransactionalCache(cache).getObject(key);
	}

	public void putObject(Cache cache, CacheKey key, Object value) {
		getTransactionalCache(cache).putObject(key, value);
	}

	public void commit() {
		for (TransactionalCache txCache : transactionCaches.values()) {
			txCache.commit();
		}
	}

	public void rollback() {
		for (TransactionalCache txCache : transactionCaches.values()) {
			txCache.rollback();
		}
	}

	private TransactionalCache getTransactionalCache(Cache cache) {
		return MapUtil.computIfAbsent(transactionCaches, cache, TransactionalCache::new);
	}
}
