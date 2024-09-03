package com.mawen.learn.mybatis.session;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public interface ResultHandler<T> {

	void handleResult(ResultContext<? extends T> resultContext);
}
