package com.mawen.learn.mybatis.cache.decorators;

import java.util.concurrent.TimeUnit;

import com.mawen.learn.mybatis.cache.Cache;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class ScheduledCache implements Cache {

	private final Cache delegate;
	protected long clearInterval;
	protected long lastClear;

	public ScheduledCache(Cache delegate) {
		this.delegate = delegate;
		this.clearInterval = TimeUnit.HOURS.toMillis(1);
		this.lastClear = System.currentTimeMillis();
	}

	public void setClearInterval(long clearInterval) {
		this.clearInterval = clearInterval;
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public void putObject(Object key, Object value) {
		clearWhenStale();
		delegate.putObject(key, value);
	}

	@Override
	public Object getObject(Object key) {
		return clearWhenStale() ? null : delegate.getObject(key);
	}

	@Override
	public Object removeObject(Object key) {
		clearWhenStale();
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		lastClear = System.currentTimeMillis();
		delegate.clear();
	}

	@Override
	public int getSize() {
		clearWhenStale();
		return delegate.getSize();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	private boolean clearWhenStale() {
		if (System.currentTimeMillis() - lastClear > clearInterval) {
			clear();
			return true;
		}
		return false;
	}
}
