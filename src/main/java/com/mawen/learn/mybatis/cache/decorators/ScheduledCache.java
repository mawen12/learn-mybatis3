package com.mawen.learn.mybatis.cache.decorators;

import java.util.concurrent.TimeUnit;

import com.mawen.learn.mybatis.cache.Cache;
import lombok.Setter;

/**
 * 调度缓存，负责定时清空缓存。无类型别名。装饰器设计模式，责任链设计模式。
 * 即使时间上过期了，但是元素仍然还会存在，只有在调用{@link #putObject}, {@link #getObject}, {@link #removeObject}时才会清理。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class ScheduledCache implements Cache {

	private final Cache delegate;
	@Setter
	protected long clearInterval;
	protected long lastClear;

	public ScheduledCache(Cache delegate) {
		this.delegate = delegate;
		this.clearInterval = TimeUnit.HOURS.toMillis(1);
		this.lastClear = System.currentTimeMillis();
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
