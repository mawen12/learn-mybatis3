package com.mawen.learn.mybatis.cache.decorators;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Deque;
import java.util.LinkedList;

import com.mawen.learn.mybatis.cache.Cache;

/**
 * 基于软引用实现的缓存管理策略。类型别名：SOFT。装饰器设计模式，责任链设计模式。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class SoftCache implements Cache {

	private final Deque<Object> hardLinksToAvoidGarbageCollection;
	private final ReferenceQueue<Object> queueOfGarbageCollectedEntries;
	private final Cache delegate;
	private int numberOfHardLinks;

	public SoftCache(Cache delegate) {
		this.delegate = delegate;
		this.numberOfHardLinks = 256;
		this.hardLinksToAvoidGarbageCollection = new LinkedList<>();
		this.queueOfGarbageCollectedEntries = new ReferenceQueue<>();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public void putObject(Object key, Object value) {
		removeGarbageCollectedItems();
		delegate.putObject(key, new SoftEntry(key, value, queueOfGarbageCollectedEntries));
	}

	@Override
	public Object getObject(Object key) {
		Object result = null;
		SoftReference<Object> softReference = (SoftReference<Object>) delegate.getObject(key);
		if (softReference != null) {
			result = softReference.get();
			if (result == null) {
				delegate.removeObject(key);
			}
			else {
				synchronized (hardLinksToAvoidGarbageCollection) {
					hardLinksToAvoidGarbageCollection.addFirst(result);
					if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
						hardLinksToAvoidGarbageCollection.removeLast();
					}
				}
			}
		}
		return result;
	}

	@Override
	public Object removeObject(Object key) {
		removeGarbageCollectedItems();
		SoftReference<Object> softReference = (SoftReference<Object>) delegate.removeObject(key);
		// TODO by mawen maybe need to remove object from hardLinksToAvoidGarbageCollection
		return softReference == null ? null : softReference.get();
	}

	@Override
	public void clear() {
		synchronized (hardLinksToAvoidGarbageCollection) {
			hardLinksToAvoidGarbageCollection.clear();
		}
		removeGarbageCollectedItems();
		delegate.clear();
	}

	@Override
	public int getSize() {
		removeGarbageCollectedItems();
		return delegate.getSize();
	}

	private void removeGarbageCollectedItems() {
		SoftEntry sv;
		while ((sv = (SoftEntry) queueOfGarbageCollectedEntries.poll()) != null) {
			delegate.removeObject(sv.key);
		}
	}

	private static class SoftEntry extends SoftReference<Object> {

		private final Object key;

		public SoftEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
			super(value, garbageCollectionQueue);
			this.key = key;
		}
	}
}
