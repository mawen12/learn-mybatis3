package com.mawen.learn.mybatis.cache.decorators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/12
 */
public class TransactionalCache implements Cache {

	private static final Log log = LogFactory.getLog(TransactionalCache.class);

	private final Cache delegate;
	private boolean clearOnCommit;
	private final Map<Object, Object> entriesToAddOnCommit;
	private final Set<Object> entriesMissedInCache;

	public TransactionalCache(Cache delegate) {
		this.delegate = delegate;
		this.entriesToAddOnCommit = new HashMap<>();
		this.entriesMissedInCache = new HashSet<>();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public void putObject(Object key, Object value) {
		entriesToAddOnCommit.put(key, value);
	}

	@Override
	public Object getObject(Object key) {
		Object object = delegate.getObject(key);
		if (object == null) {
			entriesMissedInCache.add(key);
		}
		if (clearOnCommit) {
			return null;
		}
		else {
			return object;
		}
	}

	@Override
	public Object removeObject(Object key) {
		return null;
	}

	@Override
	public void clear() {
		clearOnCommit = true;
		entriesToAddOnCommit.clear();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	public void commit() {
		if (clearOnCommit) {
			delegate.clear();
		}
		flushPendingEntries();
		reset();
	}

	public void rollback() {
		unlockMissedEntries();
		reset();
	}

	private void reset() {
		clearOnCommit = false;
		entriesToAddOnCommit.clear();
		entriesMissedInCache.clear();
	}

	private void flushPendingEntries() {
		for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
			delegate.putObject(entry.getKey(), entry.getValue());
		}
		for (Object entry : entriesMissedInCache) {
			if (!entriesToAddOnCommit.containsKey(entry)) {
				delegate.putObject(entry, null);
			}
		}
	}

	private void unlockMissedEntries() {
		for (Object entry : entriesMissedInCache) {
			try {
				delegate.removeObject(entry);
			}
			catch (Exception e) {
				log.warn("Unexpected exception while notifying a rollback to the cache adapter. "
				         + "Consider upgrading your cache adapter to the latest version. Cause: " + e);
			}
		}
	}
}
