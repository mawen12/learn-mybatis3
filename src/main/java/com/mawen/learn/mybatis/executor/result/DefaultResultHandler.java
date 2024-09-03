package com.mawen.learn.mybatis.executor.result;

import java.util.ArrayList;
import java.util.List;

import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.session.ResultContext;
import com.mawen.learn.mybatis.session.ResultHandler;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class DefaultResultHandler implements ResultHandler<Object> {

	private final List<Object> list;

	public DefaultResultHandler() {
		this.list = new ArrayList<>();
	}

	public DefaultResultHandler(ObjectFactory objectFactory) {
		this.list = objectFactory.create(List.class);
	}

	@Override
	public void handleResult(ResultContext<?> resultContext) {
		list.add(resultContext.getResultObject());
	}

	public List<Object> getResultList() {
		return list;
	}
}
