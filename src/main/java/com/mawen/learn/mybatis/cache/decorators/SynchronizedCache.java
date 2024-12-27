package com.mawen.learn.mybatis.cache.decorators;

import com.mawen.learn.mybatis.cache.Cache;

/**
 * 同步的缓存装饰器，防止多线程并发访问。无类型别名。装饰器设计模式，责任链设计模式。
 * 对于缓存的所有读写操作都是同步的。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/12
 */
public class SynchronizedCache implements Cache {

	private final Cache delegate;

	public SynchronizedCache(Cache delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public synchronized void putObject(Object key, Object value) {
		delegate.putObject(key, value);
	}

	@Override
	public synchronized Object getObject(Object key) {
		return delegate.getObject(key);
	}

	@Override
	public synchronized Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	@Override
	public synchronized void clear() {
		delegate.clear();
	}

	@Override
	public synchronized int getSize() {
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
}
