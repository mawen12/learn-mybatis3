package com.mawen.learn.mybatis.cache.decorators;

import java.lang.ref.ReferenceQueue;
import java.util.Deque;

import com.mawen.learn.mybatis.cache.Cache;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class SoftCache implements Cache {

	private final Deque<Object> hardLinksToAvoidGarbageCollection;
	private final ReferenceQueue<Object> queueOfGarbageCollectedEntries;
	private final Cache delegate;
	private int numberOfHardLinks;

	@Override
	public String getId() {
		return "";
	}

	@Override
	public void putObject(Object key, Object value) {

	}

	@Override
	public Object getObject(Object key) {
		return null;
	}

	@Override
	public Object removeObject(Object key) {
		return null;
	}

	@Override
	public void clear() {

	}

	@Override
	public int getSize() {
		return 0;
	}
}
