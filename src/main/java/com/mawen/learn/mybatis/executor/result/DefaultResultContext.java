package com.mawen.learn.mybatis.executor.result;

import com.mawen.learn.mybatis.session.ResultContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class DefaultResultContext<T> implements ResultContext<T> {

	private T resultObject;
	private int resultCount;
	private boolean stopped;

	public DefaultResultContext() {
		this.resultObject = null;
		this.resultCount = 0;
		this.stopped = false;
	}

	@Override
	public T getResultObject() {
		return resultObject;
	}

	@Override
	public int getResultCount() {
		return resultCount;
	}

	@Override
	public boolean isStopped() {
		return stopped;
	}

	public void nextResultObject(T resultObject) {
		resultCount++;
		this.resultObject = resultObject;
	}

	@Override
	public void stop() {
		this.stopped = true;
	}
}
