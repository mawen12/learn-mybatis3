package com.mawen.learn.mybatis.cache.decorators;

import java.util.ArrayDeque;
import java.util.Deque;

import com.mawen.learn.mybatis.cache.Cache;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class FifoCache implements Cache {

	private final Cache delegate;
	private final Deque<Object> keyList;
	private int size;

	public FifoCache(Cache delegate) {
		this.delegate = delegate;
		this.keyList = new ArrayDeque<>();
		this.size = 1024;
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public void putObject(Object key, Object value) {
		cycleKeyList(key);
		delegate.putObject(key, value);
	}

	@Override
	public Object getObject(Object key) {
		return delegate.getObject(key);
	}

	@Override
	public Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		delegate.clear();
		keyList.clear();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	public void setSize(int size) {
		this.size = size;
	}

	private void cycleKeyList(Object key) {
		keyList.addLast(key);
		if (keyList.size() > size) {
			Object oldestKey = keyList.removeFirst();
			delegate.removeObject(oldestKey);
		}
	}
}
