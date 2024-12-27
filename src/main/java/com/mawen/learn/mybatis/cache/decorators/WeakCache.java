package com.mawen.learn.mybatis.cache.decorators;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.LinkedList;

import com.mawen.learn.mybatis.cache.Cache;

/**
 * 基于弱引用实现的缓存管理策略。类型别名：WEAK。装饰器设计模式，责任链设计模式。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/12
 */
public class WeakCache implements Cache {

	private final Deque<Object> hardLinksToAvoidGarbageCollection;
	private final ReferenceQueue<Object> queueOfGarbageCollectedEntries;
	private final Cache delegate;
	private int numberOfHardLinks;

	public WeakCache(Cache delegate) {
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
		delegate.putObject(key, new WeakEntry(key,value, queueOfGarbageCollectedEntries));
	}

	@Override
	public Object getObject(Object key) {
		Object result = null;
		WeakReference<Object> weakReference = (WeakReference<Object>) delegate.getObject(key);
		if (weakReference != null) {
			result = weakReference.get();
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
		WeakReference<Object> weakReference  = (WeakReference<Object>) delegate.removeObject(key);
		return weakReference == null ? null : weakReference.get();
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
		return 0;
	}

	private void removeGarbageCollectedItems() {
		WeakEntry sv;
		while ((sv = (WeakEntry) queueOfGarbageCollectedEntries.poll()) != null) {
			delegate.removeObject(sv.key);
		}
	}

	private static class WeakEntry extends WeakReference<Object> {

		private final Object key;

		public WeakEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
			super(value, garbageCollectionQueue);
			this.key = key;
		}
	}
}
