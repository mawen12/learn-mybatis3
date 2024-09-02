package com.mawen.learn.mybatis.cache;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/1
 */
public interface Cache {

	String getId();

	void putObject(Object key, Object value);

	Object getObject(Object key);

	Object removeObject(Object key);

	void clear();

	int getSize();

	default ReadWriteLock getReadWriteLock() {
		return null;
	}
}
