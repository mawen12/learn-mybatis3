package com.mawen.learn.mybatis.builder;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapper;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
public class CustomObjectWrapperFactory implements ObjectWrapperFactory {

	private String option;

	@Override
	public boolean hasWrapperFor(Object object) {
		return false;
	}

	@Override
	public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
		return null;
	}
}
