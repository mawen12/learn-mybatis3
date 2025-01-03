package com.mawen.learn.mybatis.cache.decorators;

import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;

/**
 * 输出缓存命中的日志信息。无类型别名，装饰器设计模式，责任链设计模式。
 * 只有在取值的时候，并且日志级别为debug时，才会打印日志。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class LoggingCache implements Cache {
	/**
	 * 日志类，负责输出日志
	 */
	private final Log log;
	/**
	 * 装饰的缓存
	 */
	private final Cache delegate;
	/**
	 * 从缓存中读操作的累计次数
	 */
	private int requests = 0;
	/**
	 * 从缓存中读操作的累计命中次数
	 */
	private int hits = 0;

	public LoggingCache(Cache delegate) {
		this.delegate = delegate;
		this.log = LogFactory.getLog(getId());
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public void putObject(Object key, Object value) {
		delegate.putObject(key, value);
	}

	@Override
	public Object getObject(Object key) {
		requests++;
		final Object value = delegate.getObject(key);
		if (value != null) {
			hits++;
		}

		if (log.isDebugEnabled()) {
			log.debug("Cache Hit Ratio [" + getId() + "]: " + getHitRatio());
		}

		return value;
	}

	@Override
	public Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public int getSize() {
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

	private double getHitRatio() {
		return (double) hits / (double) requests;
	}

}
