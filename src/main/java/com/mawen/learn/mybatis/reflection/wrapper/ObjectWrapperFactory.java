package com.mawen.learn.mybatis.reflection.wrapper;

import com.mawen.learn.mybatis.reflection.MetaObject;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public interface ObjectWrapperFactory {

	boolean hasWrapperFor(Object object);

	ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);
}
