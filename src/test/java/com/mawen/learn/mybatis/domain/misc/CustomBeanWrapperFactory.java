package com.mawen.learn.mybatis.domain.misc;

import com.mawen.learn.mybatis.domain.blog.Author;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapper;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/23
 */
public class CustomBeanWrapperFactory implements ObjectWrapperFactory {

	@Override
	public boolean hasWrapperFor(Object object) {
		if (object instanceof Author) {
			return true;
		}
		return false;
	}

	@Override
	public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
		return new CustomBeanWrapper(metaObject,object);
	}
}
