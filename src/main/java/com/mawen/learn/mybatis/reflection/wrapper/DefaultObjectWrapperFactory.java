package com.mawen.learn.mybatis.reflection.wrapper;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.ReflectionException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory{

	@Override
	public boolean hasWrapperFor(Object object) {
		return false;
	}

	@Override
	public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
		throw new ReflectionException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
	}
}
